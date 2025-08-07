package hr.biosoft.batch.model;


import lombok.Data;

@Data
public class JobDto {
    public Long id;
    public String name;
    public String parameters;
    public String status;
    public String startTime;
    public String endTime;
}

