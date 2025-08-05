package com.example.batch.web;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;

    private final Job job;

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


            JobExecution execution = jobLauncher.run(job, jobParameters);
            return "Job started with ID: " + execution.getId();
        } catch (Exception e) {
            return "Failed to start job: " + e.getMessage();
        }
    }
}
