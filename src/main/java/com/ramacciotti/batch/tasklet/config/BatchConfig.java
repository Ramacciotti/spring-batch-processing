package com.ramacciotti.batch.tasklet.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@PropertySource("classpath:application-tasklet.properties")
public class BatchConfig {

    /**
     * 1) job()<br>
     * - Configures how the job will be created
     * <br><br>
     * 2) jobRepository <br>
     * - use the repository instance provided by springboot<br>
     * - It saves metadata that allows SpringBatch orchestrat the execution flow<br>
     * - It uses H2 database to save data
     * <br><br>
     * 3) JobBuilder<br>
     * - Defines the name of the job<br>
     * - Starts the job<br>
     * - Create a job instance<br>
     */
    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder
                ("job_name", jobRepository)
                .start(step)
                .build();
    }

    /**
     * 1) StepBuilder<br>
     * - Defines the name of the step<br>
     * - Defines what type of step will be created (in this case, tasklet)
     * <br><br>
     * 2) Tasklet<br>
     * - simple step that only executes and finishes<br>
     * - Its an interface so it needs to be implemented
     * - returns a status, in this case, FINISHED
     * <br><br>
     *  3) TransactionManager<br>
     * - defines what transactionManeger will be used inside this tasklet<br>
     * - PlatformTransactionManager its created automatically when we added the H2 Dependency
     */
    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder
                ("step_name", jobRepository)
                .tasklet((StepContribution stepContribution, ChunkContext chunkContext) -> {
                    System.out.println("Hello word!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

}
