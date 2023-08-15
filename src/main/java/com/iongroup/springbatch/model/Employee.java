package com.iongroup.springbatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    private int id;
    @NotBlank(message = "First name cannot be null or blank.")
    private String firstName;
    @NotBlank(message = "Last name cannot be null or blank.")
    private String lastName;
    @NotBlank(message = "Job cannot be null or blank.")
    private String job;
    @ManyToOne
    private Department department;
}
