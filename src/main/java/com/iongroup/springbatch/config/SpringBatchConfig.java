package com.iongroup.springbatch.config;

import com.iongroup.springbatch.consts.Constants;
import com.iongroup.springbatch.model.Customer;
import com.iongroup.springbatch.repository.CustomerRepository;
import com.iongroup.springbatch.writer.CsvFileWriter;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@AllArgsConstructor
public class SpringBatchConfig {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CsvFileWriter csvFileWriter;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public FlatFileItemReader<Customer> reader() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(Constants.UNPROCESSED + Constants.FILE));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        CustomerFieldSetMapper fieldSetMapper = new CustomerFieldSetMapper();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;

    }

    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> writer() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("csv-step",jobRepository).
                <Customer, Customer>chunk(10,transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob(JobRepository jobRepository,PlatformTransactionManager transactionManager) {
        return new JobBuilder("importCustomers",jobRepository)
                .flow(step1(jobRepository,transactionManager))
                .next(saveToCsvStep(jobRepository, transactionManager))
                .next(moveToProcessed(jobRepository, transactionManager))
                .end()
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

    @Bean
    public Step saveToCsvStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("saveToCsvStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(databaseItemReader())
                .writer(csvFileWriter)
                .build();
    }

    @Bean
    public Step moveToProcessed(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("moveToProcessed", jobRepository)
                .tasklet(fileMoveTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet fileMoveTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                moveFile();
                return RepeatStatus.FINISHED;
            }
        };
    }

    private void moveFile() throws IOException {

        Path sourcePath = Paths.get(Constants.UNPROCESSED, Constants.FILE);
        Path destinationPath = Paths.get(Constants.PROCESSED, Constants.getParsedFileName());

        Files.move(sourcePath, destinationPath);
    }

    @Bean
    public JpaPagingItemReader<Customer> databaseItemReader() {
        JpaPagingItemReader<Customer> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT c FROM Customer c");
        reader.setPageSize(10); // Adjust the page size as needed
        return reader;
    }

}