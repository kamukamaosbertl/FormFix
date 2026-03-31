package com.example.smartfit.device.model;

import java.util.ArrayList;
import java.util.List;

public class InfoCategory {
    private final String title;
    private final int headerColorRes; //bacjground shade resource
    private final List<InfoRow> rows;

    public InfoCategory(String title, int headerColorRes){
        this.title = title;
        this.headerColorRes = headerColorRes;
        this.rows = new ArrayList<>();
    }

    public InfoCategory add(String key, String value){
        getRows().add(new InfoRow(key, value));
        return this;
    }


    public String getTitle() {
        return title;
    }

    public int getHeaderColorRes() {
        return headerColorRes;
    }

    public List<InfoRow> getRows() {
        return rows;
    }
}
