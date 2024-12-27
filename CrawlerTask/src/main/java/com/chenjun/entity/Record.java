package com.chenjun.entity;

import lombok.Data;

@Data
public class Record  /*implements java.io.Serializable*/ {
    //id
    private Integer id;

    //电子申请方式
    private String apply_for;

    //电子申请代码
    private String dianzisqfs;

    //发文序列号
    private String fawenxlh;

    //发明创造名称
    private String zhuanlimc;

    //接受账户
    private String accept;

    //专利申请号
    private String zhuanlisqh;

    //通知书名称
    private String tongzhismc;

    //接受账户id
    private String shoujianrid;

    //通知书发文日
    private String dianzifwrq;

    //null
    private String tongzhislx;

    //null
    private String guojisqh;

    //null
    private String anjianbh;

    //null
    private String rid;

    //null
    private String dianzisqlx;
}
