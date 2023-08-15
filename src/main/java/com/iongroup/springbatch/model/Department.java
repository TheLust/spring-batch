package com.iongroup.springbatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {
    @Id
    private int id;
    @NotBlank(message = "Name cannot be null or blank.")
    private String name;
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
}
