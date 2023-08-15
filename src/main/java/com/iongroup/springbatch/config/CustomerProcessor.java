package com.iongroup.springbatch.config;

import com.iongroup.springbatch.consts.Constants;
import com.iongroup.springbatch.model.Customer;
import com.iongroup.springbatch.repository.CustomerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.batch.item.ItemProcessor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;


public class CustomerProcessor implements ItemProcessor<Customer, Customer> {

    CustomerRepository customerRepository;

    @Override
    public Customer process(Customer item) throws Exception {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Customer>> constraintViolations = validator.validate(item);

        if (!constraintViolations.isEmpty()) {
            writeCustomerWithViolations(item, constraintViolations);
            return null;
        }

        // Your processing logic here
        return item;
    }

    public void writeCustomerWithViolations(Customer customer, Set<ConstraintViolation<Customer>> violations) {
        String csvFilePath = Constants.REJECTED + "rejected_" + Constants.getParsedFileName();

        try (FileWriter writer = new FileWriter(csvFilePath, true)) {
            StringBuilder csvLine = new StringBuilder();

            // Append customer fields
            csvLine.append(customer.getId()).append(",")
                    .append(customer.getFirstName()).append(",")
                    .append(customer.getLastName()).append(",")
                    .append(customer.getEmail()).append(",")
                    .append(customer.getGender()).append(",")
                    .append(customer.getContactNo()).append(",")
                    .append(customer.getCountry()).append(",")
                    .append(customer.getDob()).append(",");

            // Append violation messages
            StringBuilder violationMessages = new StringBuilder();
            for (ConstraintViolation<Customer> violation : violations) {
                violationMessages.append(violation.getMessage()).append("; ");
            }
            csvLine.append("\"").append(violationMessages).append("\"").append("\n");

            writer.append(csvLine.toString());
        } catch (IOException ignored) {}
    }
}
