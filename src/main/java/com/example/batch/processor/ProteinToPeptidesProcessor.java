
package com.example.batch.processor;

import com.example.batch.model.ProteinEntry;
import com.example.batch.model.Peptide;
import com.example.batch.model.ProcessedPeptides;
import org.springframework.batch.item.ItemProcessor;

import java.util.ArrayList;
import java.util.List;

public class ProteinToPeptidesProcessor implements ItemProcessor<ProteinEntry, ProcessedPeptides> {

    @Override
    public ProcessedPeptides process(ProteinEntry protein) {
        if (protein == null || protein.getSequence() == null) return null;

        List<Peptide> peptides = digest(protein.getSequence());

        return new ProcessedPeptides(protein.getAccession(), protein.getTaxonomyId(), peptides);
    }

    private List<Peptide> digest(String sequence) {
        List<Peptide> peptides = new ArrayList<>();
        int start = 0;

        for (int i = 0; i < sequence.length(); i++) {
            char current = sequence.charAt(i);
            char next = (i + 1 < sequence.length()) ? sequence.charAt(i + 1) : '-';

            // Trypsin cleavage: after K or R, not before P
            if ((current == 'K' || current == 'R') && next != 'P') {
                String pep = sequence.substring(start, i + 1);
                if (!pep.isBlank()) {
                    peptides.add(new Peptide(pep));
                }
                start = i + 1;
            }
        }

        // Add remaining tail
        if (start < sequence.length()) {
            String tail = sequence.substring(start);
            if (!tail.isBlank()) {
                peptides.add(new Peptide(tail));
            }
        }

        return peptides;
    }
}
