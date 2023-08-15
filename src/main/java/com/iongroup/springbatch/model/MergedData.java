package com.iongroup.springbatch.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MergedData {
    private int id;
    private String firstName;
    private String lastName;
    private String job;
    private String department;
}
