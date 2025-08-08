package hr.biosoft.batch.web;

import hr.biosoft.batch.model.JobDto;
import hr.biosoft.batch.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.configuration.JobRegistry;
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
    private final JobRegistry jobRegistry;


    @GetMapping("/jobs")
    public List<JobDto> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/job/status")
    public Map<String, Object> getLatestJobStatus(@RequestParam String jobName) {
        Map<String, Object> status = new HashMap<>();
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 1);
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
    public String startJobWithParams(@RequestParam String jobName,
                                     @RequestParam Map<String, String> allParams) {
        try {
            Job job = jobRegistry.getJob(jobName);

            JobParametersBuilder builder = new JobParametersBuilder();
            allParams.forEach((key, value) -> {
                if (!key.equals("jobName")) {
                    builder.addString(key, value);
                }
            });
            builder.addLong("timestamp", System.currentTimeMillis()); // unikatan parametar

            JobParameters jobParameters = builder.toJobParameters();

            log.debug("Pokrećem job '{}' s parametrima: {}", jobName, jobParameters);

            JobExecution execution = jobLauncher.run(job, jobParameters);
            return "Job '" + jobName + "' pokrenut s ID: " + execution.getId();
        } catch (Exception e) {
            return "Neuspješno pokretanje joba '" + jobName + "': " + e.getMessage();
        }
    }
}
