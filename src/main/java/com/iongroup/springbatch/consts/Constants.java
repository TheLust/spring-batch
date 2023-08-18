package com.iongroup.springbatch.consts;

import java.time.LocalDate;

public class Constants {
    private static final String RESOURCES = "src/main/resources/";
    public static final String UNPROCESSED = RESOURCES + "unprocessed/";
    public static final String PROCESSED = RESOURCES + "processed/";
    public static final String REJECTED = RESOURCES+  "rejected/";
    public static final String COMPLETED = RESOURCES + "completed/";
    public static final String FILE = "customers.csv";

    public static String getParsedFileName() {
        String nameWithoutExtension = FILE.substring(0, FILE.lastIndexOf('.'));
        String extension = FILE.substring(FILE.lastIndexOf('.'));

        return nameWithoutExtension + "_" + LocalDate.now() + extension;
    }
}
