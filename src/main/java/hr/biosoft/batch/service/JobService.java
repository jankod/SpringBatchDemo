package hr.biosoft.batch.service;

import hr.biosoft.batch.model.JobDto;
import org.springframework.stereotype.Service;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobExplorer jobExplorer;

    public List<JobDto> getAllJobs() {
        List<JobDto> jobs = new ArrayList<>();
        // Dohvati sva imena poslova
        List<String> jobNames = jobExplorer.getJobNames();
        for (String jobName : jobNames) {
            // Za svako ime posla, dohvati sve instance
            List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);
            for (JobInstance instance : instances) {
                // Za svaku instancu, dohvati sve egzekucije
                List<JobExecution> executions = jobExplorer.getJobExecutions(instance)
                      .stream().collect(Collectors.toList());
                for (JobExecution execution : executions) {
                    JobDto dto = new JobDto();
                    dto.id = execution.getId();
                    dto.name = jobName;
                    dto.parameters = execution.getJobParameters().toString();
                    dto.status = execution.getStatus() != null ? execution.getStatus().toString() : "";
                    dto.startTime = execution.getStartTime() != null ? execution.getStartTime().toString() : "";
                    dto.endTime = execution.getEndTime() != null ? execution.getEndTime().toString() : "";
                    jobs.add(dto);
                }
            }
        }
        return jobs;
    }
}
