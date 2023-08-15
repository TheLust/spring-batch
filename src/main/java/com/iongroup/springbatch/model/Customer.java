package com.iongroup.springbatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    @Id
    private int id;
    @NotBlank(message = "First name cannot be null or blank.")
    private String firstName;

    @NotBlank(message = "Last name cannot be null or blank.")
    private String lastName;

    @NotBlank(message = "Email cannot be null or blank.")
    @Email(message = "Invalid email.")
    private String email;

    @NotNull(message = "First name cannot be null.")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotBlank(message = "Contact number cannot be null or blank.")
    private String contactNo;

    @NotBlank(message = "Country cannot be null or blank.")
    private String country;

    @NotNull(message = "Date of birth cannot be null.")
    @Past(message = "Date of birth must be in the past.")
    private LocalDate dob;
}
