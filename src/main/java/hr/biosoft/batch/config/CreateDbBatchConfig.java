package hr.biosoft.batch.config;

import hr.biosoft.batch.model.ProcessedPeptides;
import hr.biosoft.batch.model.ProteinEntry;
import hr.biosoft.batch.param.JobParams;
import hr.biosoft.batch.processor.ProteinToPeptidesProcessor;
import hr.biosoft.batch.reader.ProteinStaxReader;
import hr.biosoft.batch.writer.PeptideCsvWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobExecution;
import org.springframework.util.StopWatch;
import hr.biosoft.batch.util.TimeFormatter;

@Slf4j
@Configuration
@EnableBatchProcessing()
public class CreateDbBatchConfig {


    @Bean
    @StepScope
    public JobParams jobParams(
          @Value("#{jobParameters['inputPath']}") String inputPath,
          @Value("#{jobParameters['outputPath']}") String outputPath) {
        return new JobParams(inputPath, outputPath);
    }

//    @BatchDataSource
//    @Bean
//    public DataSource dataSource() {
//        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
//
//        return builder.setType(EmbeddedDatabaseType.H2)
//              .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
//              .addScript("classpath:org/springframework/batch/core/
//              )
//              .setName("/home/tag/spring_batch_db.mv.db")
//              .build();
//    }

//    @Bean
//    public JobRepository jobRepository(DataSource batchDataSource, PlatformTransactionManager transactionManager) throws Exception {
//        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//        factory.setDataSource(batchDataSource);
//        factory.setTransactionManager(transactionManager);
//        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
//        factory.setTablePrefix("BATCH_");
//        factory.afterPropertiesSet();
//        return factory.getObject();
//    }

//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
//        return new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource);
//    }

    @Bean
    @StepScope
    public ProteinStaxReader reader(@Value("#{jobParameters['inputPath']}") String inputPath) {
        log.info("Creating ProteinStaxReader with inputPath: {}", inputPath);
        return new ProteinStaxReader(inputPath);
    }

    @Bean
    @StepScope
    public PeptideCsvWriter writer(JobParams params, List<String> progressLog) throws Exception {
        return new PeptideCsvWriter(params.getOutputCsvPath(), progressLog, 100);
    }

    @Bean
    public ProteinToPeptidesProcessor processor() {
        return new ProteinToPeptidesProcessor();
    }

    @Bean
    public Step processXmlStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ProteinStaxReader reader,
                               ProteinToPeptidesProcessor processor,
                               PeptideCsvWriter writer,
                               TaskExecutor taskExecutor) {
        return new StepBuilder("processXmlStep", jobRepository)
              .<ProteinEntry, ProcessedPeptides>chunk(1, transactionManager)
              .reader(reader)
              .processor(processor)
              .writer(writer)
              //.taskExecutor(taskExecutor)
              .build();
    }

    @Bean
    public Job processJob(JobRepository jobRepository, Step processXmlStep) {
        return new JobBuilder("processXmlJob", jobRepository)
              .incrementer(new RunIdIncrementer())
              .listener(jobExecutionListener())
              .start(processXmlStep)
              .build();
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            private StopWatch jobStopWatch;

            @Override
            public void beforeJob(JobExecution jobExecution) {
                jobStopWatch = new StopWatch("Job Execution");
                jobStopWatch.start();
                log.info("Starting job: {}", jobExecution.getJobInstance().getJobName());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobStopWatch != null) {
                    jobStopWatch.stop();
                    log.info("Job {} completed in: {} with status: {}",
                          jobExecution.getJobInstance().getJobName(),
                          TimeFormatter.formatStopWatch(jobStopWatch),
                          jobExecution.getStatus());
                }
            }
        };
    }


    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(5);
        exec.setMaxPoolSize(5);
        exec.setQueueCapacity(10); // Maksimalno 10 chunkova može čekati
        exec.setThreadNamePrefix("xml-thread-");
        exec.initialize();
        return exec;
    }
}
