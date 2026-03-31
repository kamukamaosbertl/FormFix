package com.example.smartfit.device.model;

public class InfoRow {
    private final String key;
    private String value;

    public InfoRow(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
