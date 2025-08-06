package com.example.batch.reader;

import com.example.batch.model.ProteinEntry;
import com.example.batch.util.TimeFormatter;
import io.micrometer.core.instrument.util.StringEscapeUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

@Slf4j
public class ProteinStaxReader extends AbstractItemStreamItemReader<ProteinEntry> {
    private XMLStreamReader reader;
    private final String filePath;

    int proteinCount = 0;
    private StopWatch stopWatch;
    private int processedCount = 0;

    public ProteinStaxReader(String filePath) {
        this.filePath = filePath;
        initializeReader();
    }

    private void initializeReader() {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStream inputStream;

            // Check if file is gzipped by extension
            if (filePath.endsWith(".gz")) {
                inputStream = new GZIPInputStream(new BufferedInputStream(fis));
            } else {
                inputStream = new BufferedInputStream(fis);
            }

            InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            reader = factory.createXMLStreamReader(isr);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize XML reader", e);
        }
    }

    @Override
    public void open(ExecutionContext executionContext) {
        super.open(executionContext);
        stopWatch = new StopWatch("XML Processing");
        stopWatch.start("Initialization");
        // ...existing code...
        stopWatch.stop();
        log.info("Initialization completed in: {}", TimeFormatter.formatStopWatch(stopWatch));
        stopWatch.start("Processing");
    }

    public ProteinEntry read() throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT && "entry".equals(reader.getLocalName())) {
                return parseEntry();
            }

//            if (proteinCount > 1000) {
//                log.info("Protein count is too large, stopping read.");
//                return null; // Limit to 1000 proteins for testing
//            }
        }
        return null;
    }

    private ProteinEntry parseEntry() throws XMLStreamException {

        proteinCount++;
        String accession = null;
        String sequence = null;
        String taxId = null;

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT) {
                String elementName = reader.getLocalName();

                switch (elementName) {
                    case "accession":
                        if (accession == null) { // Take first accession as primary
                            accession = reader.getElementText();
                        }
                        break;

                    case "sequence":
                        sequence = StringUtils.deleteWhitespace(reader.getElementText());
                        break;
                    case "organism":
                        taxId = parseOrganism();
                        break;
                }
            }

            if (event == XMLStreamReader.END_ELEMENT && "entry".equals(reader.getLocalName())) {
                break; // End of current entry
            }
        }

        if (accession != null && sequence != null && taxId != null) {
            return new ProteinEntry(accession, sequence, taxId);
        }

        return null; // Skip entries without required data
    }

    private String parseOrganism() throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT && "dbReference".equals(reader.getLocalName())) {
                if ("NCBI Taxonomy".equals(reader.getAttributeValue(null, "type"))) {
                    return reader.getAttributeValue(null, "id");
                }
            }

            if (event == XMLStreamReader.END_ELEMENT && "organism".equals(reader.getLocalName())) {
                break; // End of organism tag
            }
        }
        return null;
    }

    @SneakyThrows
    public void close() {
        if (reader != null) {
            reader.close();
        }
        if (stopWatch != null && stopWatch.isRunning()) {
            stopWatch.stop();
            log.info("Total processing time: {} for {} proteins",
                    TimeFormatter.formatStopWatch(stopWatch), processedCount);
        }
    }
}
