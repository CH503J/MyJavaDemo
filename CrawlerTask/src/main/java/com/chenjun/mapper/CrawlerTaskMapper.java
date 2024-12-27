package com.chenjun.mapper;

import com.chenjun.entity.Record;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CrawlerTaskMapper {
    void saveRecord(Record record);
}
