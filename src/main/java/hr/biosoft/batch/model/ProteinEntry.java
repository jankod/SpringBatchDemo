package hr.biosoft.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProteinEntry {
    private String accession;
    private String sequence;
    private String taxonomyId;
}
