package com.chenjun.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Result implements Serializable {
    private List<Record> records;

    // Getters and setters
    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }
}
