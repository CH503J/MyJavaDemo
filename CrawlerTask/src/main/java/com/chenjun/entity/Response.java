package com.chenjun.entity;


import lombok.Data;

@Data
public class Response {
    private int code;

    private String message;

    private Result result;
}

