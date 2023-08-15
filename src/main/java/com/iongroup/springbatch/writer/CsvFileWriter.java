package com.iongroup.springbatch.writer;

import com.iongroup.springbatch.consts.Constants;
import com.iongroup.springbatch.model.Customer;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class CsvFileWriter implements ItemWriter<Customer> {

    @Override
    public void write(Chunk<? extends Customer> items) throws Exception {
        String csvFilePath = Constants.COMPLETED + "completed_" + Constants.getParsedFileName();
        for (Customer customer : items) {
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
                writer.append(csvLine.toString());
                writer.append("\n");
            } catch (IOException ignored) {}
        }
    }
}