package com.iongroup.springbatch.config;

import com.iongroup.springbatch.model.Customer;
import com.iongroup.springbatch.model.Gender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomerFieldSetMapper extends BeanWrapperFieldSetMapper<Customer> {
    @Override
    public Customer mapFieldSet(FieldSet fieldSet) throws BindException {
        Customer customer = new Customer();

        customer.setId(fieldSet.readInt("id"));
        String firstName = fieldSet.readString("firstName");
        String lastName = fieldSet.readString("lastName");
        customer.setFirstName(transformName(firstName));
        customer.setLastName(transformName(lastName));
        customer.setEmail(fieldSet.readString("email"));
        customer.setGender(mapGender(fieldSet.readString("gender")));
        customer.setContactNo(fieldSet.readString("contactNo"));
        customer.setCountry(fieldSet.readString("country"));

        // Parse date from string to LocalDate
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate dob = LocalDate.parse(fieldSet.readString("dob"), dateFormatter);
        customer.setDob(dob);

        return customer;
    }

    private Gender mapGender(String genderValue) {
        if ("MALE".equalsIgnoreCase(genderValue)) {
            return Gender.MALE;
        } else if ("FEMALE".equalsIgnoreCase(genderValue)) {
            return Gender.FEMALE;
        } else {
            return Gender.OTHER;
        }
    }

    private String transformName(String name) {
        if (name != null && !name.isEmpty()) {
            return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        return name;
    }
}
