package hr.biosoft.batch.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDbJobParams {
    private String inputXmlGzPath;
    private String outputCsvPath;
}
