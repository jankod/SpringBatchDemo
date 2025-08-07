
package hr.biosoft.batch.writer;

import hr.biosoft.batch.model.Peptide;
import hr.biosoft.batch.model.ProcessedPeptides;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PeptideCsvWriter implements ItemWriter<ProcessedPeptides> {

    private final BufferedWriter writer;
    private final AtomicInteger proteinCounter = new AtomicInteger(0);
    private final AtomicInteger peptideCounter = new AtomicInteger(0);
    private final List<String> progressLog;

    private final int logEveryN;

    public PeptideCsvWriter(String outputCsvPath, List<String> progressLog, int logEveryN) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(outputCsvPath));
        this.progressLog = progressLog;
        this.logEveryN = logEveryN;
        writer.write("proteinAccession,taxonomyId,peptideSequence\n");
    }

    @Override
    public synchronized void write(Chunk<? extends ProcessedPeptides> items) throws Exception {

        // public synchronized void write(List<? extends ProcessedPeptides> items) throws Exception {
        for (ProcessedPeptides pp : items) {
            for (Peptide p : pp.getPeptides()) {
                writer.write(pp.getAccession() + "," + pp.getTaxonomyId() + "," + p.getSequence() + "\n");
                peptideCounter.incrementAndGet();
            }

            int count = proteinCounter.incrementAndGet();
            if (count % logEveryN == 0) {
                progressLog.add("Processed " + count + " proteins, " + peptideCounter.get() + " peptides total.");
            }
        }
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }

    public int getTotalProteins() {
        return proteinCounter.get();
    }

    public int getTotalPeptides() {
        return peptideCounter.get();
    }


}
