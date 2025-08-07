package hr.biosoft.batch.web;

import hr.biosoft.batch.model.JobDto;
import hr.biosoft.batch.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController()
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobController {

    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;
    private final JobService jobService;

    private final Job job;


    @GetMapping("/jobs")
    public List<JobDto> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/job/status")
    public Map<String, Object> getLatestJobStatus() {
        Map<String, Object> status = new HashMap<>();
        List<JobInstance> instances = jobExplorer.getJobInstances("processXmlJob", 0, 1);
        if (instances.isEmpty()) {
            status.put("status", "NO_JOB_FOUND");
            return status;
        }

        JobInstance instance = instances.get(0);
        List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
        if (executions.isEmpty()) {
            status.put("status", "NO_EXECUTION_FOUND");
            return status;
        }

        JobExecution latestExecution = executions.get(0);
        status.put("status", latestExecution.getStatus().toString());
        status.put("startTime", latestExecution.getStartTime());
        status.put("endTime", latestExecution.getEndTime());
        status.put("exitStatus", latestExecution.getExitStatus().getExitCode());

        return status;
    }

    @PostMapping("/job/start")
    public String startJobWithParams(@RequestParam String inputPath,
                                     @RequestParam String outputPath) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                  .addString("inputPath", inputPath)
                  .addString("outputPath", outputPath)
                  .addLong("timestamp", System.currentTimeMillis()) // kako bi bio unikatan
                  .toJobParameters();

            log.debug("Starting job with parameters: {}", jobParameters);


            JobExecution execution = jobLauncher.run(job, jobParameters);
            return "Job started with ID: " + execution.getId();
        } catch (Exception e) {
            return "Failed to start job: " + e.getMessage();
        }
    }
}

