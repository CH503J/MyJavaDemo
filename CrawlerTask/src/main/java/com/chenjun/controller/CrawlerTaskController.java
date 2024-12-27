package com.chenjun.controller;

import com.chenjun.entity.Response;
import com.chenjun.service.CrawlerTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawler")
public class CrawlerTaskController {

    @Autowired
    private CrawlerTaskService crawlerTaskService;

    @GetMapping()
    public Response fetchData(){
        return crawlerTaskService.fetchData();
    }
}
