package com.example.batch.reader;

import com.example.batch.model.ProteinEntry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;

import java.io.*;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

@Slf4j
public class ProteinStaxReader extends AbstractItemStreamItemReader<ProteinEntry> {
    private XMLStreamReader reader;
    private final String filePath;

    int proteinCount = 0;

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

            // Use PushbackInputStream to handle BOM and leading whitespace
            PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 128);

            // Check for BOM and skip if present
            byte[] bom = new byte[3];
            int bytesRead = pushbackInputStream.read(bom);

            if (bytesRead >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
                // UTF-8 BOM found, don't push back
            } else {
                // No BOM, push back what we read
                pushbackInputStream.unread(bom, 0, bytesRead);
            }

            // Skip any leading whitespace before XML declaration
            ByteArrayOutputStream leading = new ByteArrayOutputStream();
            int b;
            boolean foundStart = false;
            while ((b = pushbackInputStream.read()) != -1) {
                if (!Character.isWhitespace(b)) {
                    pushbackInputStream.unread(b);
                    foundStart = true;
                    break;
                }
                leading.write(b);
            }
            if (!foundStart) {
                throw new IOException("No XML content found in file.");
            }

            InputStreamReader isr = new InputStreamReader(pushbackInputStream, "UTF-8");

            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            reader = factory.createXMLStreamReader(isr);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize XML reader", e);
        }
    }

    public ProteinEntry read() throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT && "entry".equals(reader.getLocalName())) {
                return parseEntry();
            }

//            if (event == XMLStreamReader.END_DOCUMENT) {
//                return null; // End of file
//            }
            if (proteinCount > 1000) {
                log.info("Protein count is too large, stopping read.");
                return null; // Limit to 1000 proteins for testing
            }
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
                        sequence = reader.getElementText().replaceAll("\\s", ""); // Remove whitespace
                        break;
                    case "organism":
                        taxId = parseOrganism();
                        break;

//                    case "dbReference":
//                        if ("NCBI Taxonomy".equals(reader.getAttributeValue(null, "type"))) {
//                            taxId = reader.getAttributeValue(null, "id");
//                        }
//                        break;
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
    }
}
