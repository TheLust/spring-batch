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
    public void write(Chunk<? extends Customer> items) {
        String csvFilePath = Constants.COMPLETED + "completed_" + Constants.getParsedFileName();
        for (Customer customer : items) {
            try (FileWriter writer = new FileWriter(csvFilePath, true)) {

                // Append customer fields
                String csvLine = customer.getId() + "," +
                        customer.getFirstName() + "," +
                        customer.getLastName() + "," +
                        customer.getEmail() + "," +
                        customer.getGender() + "," +
                        customer.getContactNo() + "," +
                        customer.getCountry() + "," +
                        customer.getDob() + ",";
                writer.append(csvLine);
                writer.append("\n");
            } catch (IOException ignored) {}
        }
    }
}