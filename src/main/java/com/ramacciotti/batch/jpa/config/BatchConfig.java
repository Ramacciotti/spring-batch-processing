package com.ramacciotti.batch.jpa.config;

import com.ramacciotti.batch.jpa.entity.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:application-jpa.properties")
public class BatchConfig {

    @Autowired
    @Qualifier("transactionManagerApp")
    private PlatformTransactionManager transactionManager;

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
     * - Create a job instance
     * <br><br>
     * 4) RunIncrementer<br>
     * - For each combination of identifying job parameters, you can only have one JobExecution that results in COMPLETE.<br>
     * - A RunIdIncrementer will append an additional, unique parameter to the list of parameters so that the resulting combination would be unique<br>
     * - Giving you a new JobInstance each time you ran the job with the same combination of identifying parameters.<br>
     */
    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder
                ("job_name", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
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
     * 3) TransactionManager<br>
     * - defines what transactionManeger will be used inside this tasklet<br>
     * - PlatformTransactionManager its created automatically when we added the database dependency
     */
    @Bean
    public Step step(JobRepository jobRepository, ItemReader<Person> reader, ItemWriter<Person> writer) {
        return new StepBuilder("step_name", jobRepository)
                .<Person, Person>chunk(200, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("reader")
                .resource(new FileSystemResource("src/main/resources/person.csv"))
                .comments("--")
                .delimited()
                .names("name", "email", "age", "id")
                .targetType(Person.class)
                .build();
    }

    @Bean
    public ItemWriter<Person> writer(@Qualifier("getDataSource") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .dataSource(dataSource)
                .sql(
                        "INSERT INTO person (id, name, email, age) VALUES (:id, :name, :email, :age)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

}
