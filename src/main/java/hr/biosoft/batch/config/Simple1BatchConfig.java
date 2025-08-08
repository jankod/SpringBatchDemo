package hr.biosoft.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class Simple1BatchConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job simpleJob() {
        Step simpleStep = simpleStep();
        return new JobBuilder("simpleJob", jobRepository)
                .start(simpleStep)
                .build();
    }

    @Bean
    public Step simpleStep() {
        return new StepBuilder("simpleStep", jobRepository)
                .tasklet(simpleTasklet(), transactionManager)

                .build();
    }

    @Bean
    public Tasklet simpleTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {


                String param = (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("parametar");
                System.out.println("Pokrenut job s parametrom: " + param);
                for (int i = 1; i <= 10; i++) {
                    System.out.println("Radim... " + i + "/10");
                    Thread.sleep(1000); // 1 sekunda
                }
                System.out.println("Job završen!");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
