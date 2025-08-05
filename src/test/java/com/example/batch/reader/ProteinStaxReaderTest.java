package com.example.batch.reader;

import com.example.batch.model.ProteinEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

class ProteinStaxReaderTest {

    @Test
    void testReadFirstProteinEntry() throws Exception {
        // Učitaj testni XML (.gz) iz resources

        //URL resource = getClass().getClassLoader().getResource("/media/tag/D/digested-db/uniprot_sprot.xml.gz");
        //Assertions.assertNotNull(resource, "Test XML file not found in resources.");
        //File file = new File(resource.toURI());
        File file = new File("/media/tag/D/digested-db/uniprot_sprot.xml.gz");
        ProteinStaxReader reader = new ProteinStaxReader(file.getAbsolutePath());

        ProteinEntry entry = reader.read();

        Assertions.assertNotNull(entry, "First protein entry should not be null");
        Assertions.assertNotNull(entry.getAccession(), "Accession should not be null");
        Assertions.assertNotNull(entry.getSequence(), "Sequence should not be null");
        Assertions.assertNotNull(entry.getTaxonomyId(), "Taxonomy ID should not be null");

        System.out.println("Accession: " + entry.getAccession());
        System.out.println("Taxonomy ID: " + entry.getTaxonomyId());
        System.out.println("Sequence (first 30 aa): " + entry.getSequence().substring(0, Math.min(30, entry.getSequence().length())));
        System.out.println("Sequence length: " + entry.getSequence().length());
    }
}
