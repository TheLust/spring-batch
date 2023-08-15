package com.iongroup.springbatch.config;

import com.iongroup.springbatch.consts.Constants;
import com.iongroup.springbatch.model.Customer;
import com.iongroup.springbatch.model.Employee;
import com.iongroup.springbatch.model.MergedData;
import com.iongroup.springbatch.repository.CustomerRepository;
import com.iongroup.springbatch.writer.CsvFileWriter;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
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
public class SpringBatchConfig {

    private final CustomerRepository customerRepository;

    private final CsvFileWriter csvFileWriter;

    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    public SpringBatchConfig(CustomerRepository customerRepository, CsvFileWriter csvFileWriter, EntityManagerFactory entityManagerFactory) {
        this.customerRepository = customerRepository;
        this.csvFileWriter = csvFileWriter;
        this.entityManagerFactory = entityManagerFactory;
    }

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
    public Job runJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
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
        return (contribution, chunkContext) -> {
            moveFile();
            return RepeatStatus.FINISHED;
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

    //Task 5

    @Bean
    public JpaPagingItemReader<Employee> employeeReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<Employee> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT e FROM Employee e");
        // Set other properties like pageSize, etc.
        return reader;
    }

    @Bean
    public ItemProcessor<Employee, MergedData> employeeProcessor() {
        return employee -> new MergedData(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getJob(),
                employee.getDepartment().getName()
        );
    }


    @Bean
    public FlatFileItemWriter<MergedData> csvWriter() {
        return new FlatFileItemWriterBuilder<MergedData>()
                .name("mergedDataCsvWriter")
                .resource(new FileSystemResource(Constants.PROCESSED + "output.csv"))
                .delimited()
                .names("id", "firstName", "lastName", "job", "department")
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(",");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "firstName", "lastName", "job", "department"});
                            }
                        });
                    }
                })
                .build();
    }


    @Bean
    public Step writeFromTwoTables(ItemReader<Employee> employeeReader,
                                   ItemProcessor<Employee, MergedData> employeeProcessor,
                                   ItemWriter<MergedData> writer,
                                   JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("writeFromTwoTables", jobRepository)
                .<Employee, MergedData>chunk(10, transactionManager)
                .reader(employeeReader)
                .processor(employeeProcessor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job task5(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("task5", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(writeFromTwoTables(
                        employeeReader(entityManagerFactory),
                        employeeProcessor(),
                        csvWriter(),
                        jobRepository,
                        transactionManager)
                )
                .end()
                .build();
    }
}