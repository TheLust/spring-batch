package com.iongroup.springbatch.consts;

import java.time.LocalDate;

public class Constants {
    public static final String UNPROCESSED = "src/main/resources/unprocessed/";
    public static final String PROCESSED = "src/main/resources/processed/";
    public static final String REJECTED = "src/main/resources/rejected/";
    public static final String COMPLETED = "src/main/resources/completed/";
    public static final String FILE = "customers.csv";

    public static String getParsedFileName() {
        String nameWithoutExtension = FILE.substring(0, FILE.lastIndexOf('.'));
        String extension = FILE.substring(FILE.lastIndexOf('.'));

        return nameWithoutExtension + "_" + LocalDate.now() + extension;
    }
}
