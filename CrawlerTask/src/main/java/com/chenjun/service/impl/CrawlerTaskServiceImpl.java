package com.chenjun.service.impl;

import com.chenjun.constants.CrawlerTaskConstant;
import com.chenjun.entity.Record;
import com.chenjun.entity.Response;
import com.chenjun.mapper.CrawlerTaskMapper;
import com.chenjun.service.CrawlerTaskService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class CrawlerTaskServiceImpl implements CrawlerTaskService {

    private static final Logger log = LoggerFactory.getLogger(CrawlerTaskServiceImpl.class);

    @Autowired
    private CrawlerTaskMapper crawlerTaskMapper;

    @Autowired
    private RedisService redisService;

    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public Response fetchData() {

        //每页条数
        int size = 500;
        //初始化页码
        int totalPages = Integer.MAX_VALUE;
        // 已存入的数据条数
        int totalSaved = 0;

        try {
            for (int current = 1; current <= totalPages; current++) {
                log.info("正在爬取第{}页的数据", current);
                String url = CrawlerTaskConstant.API_URL
                        .replace("size", "size=" + size)
                        .replace("current=1", "current=" + current);
                URL apiUrl = new URL(url);
                //创建一个CrawlerTaskConstant.API_URL链接
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                //将token和请求方式get放入请求头中
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", CrawlerTaskConstant.TOKEN);
                //获取响应码
                int responseCode = urlConnection.getResponseCode();
                //判断响应码是否为200
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    //使用BufferedReader读取urlConnection获取的输入流（读取响应的文本数据）
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // 解析 JSON 响应体
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    int code = jsonResponse.getInt("code");
                    String message = jsonResponse.getString("message");
                    long timestamp = jsonResponse.getLong("timestamp");
                    Date date = new Date(timestamp);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateTime = dateFormat.format(date);

                    log.info("响应码: " + code);
                    log.info("请求结果: " + message);
                    log.info("请求时间: " + dateTime);

                    JSONObject resultObject = jsonResponse.getJSONObject("result");
                    JSONArray recordsArray = resultObject.getJSONArray("records");

                    if (recordsArray.length() == 0) {
                        log.info("没有更多数据，爬取结束");
                        break;
                    }

                    for (int i = 0; i < recordsArray.length(); i++) {
                        JSONObject recordObject = recordsArray.getJSONObject(i);


                        String zhuanlisqh = recordObject.optString("zhuanlisqh", "N/A");

                        String uniqueKey = recordObject.optString("rid", "N/A");

                        if (redisService.addUniqueRecord("uniqueRecords", uniqueKey)) {
                            Record recordEntity = new Record();

                            // 设置 Record 实体的属性
                            recordEntity.setDianzisqfs(recordObject.optString("dianzisqfs", "N/A"));
                            recordEntity.setDianzisqlx(recordObject.optString("dianzisqlx", "N/A"));
                            recordEntity.setFawenxlh(recordObject.optString("fawenxlh", "N/A"));
                            recordEntity.setTongzhislx(recordObject.optString("tongzhislx", "N/A"));
                            recordEntity.setGuojisqh(recordObject.optString("guojisqh", "N/A"));
                            recordEntity.setAnjianbh(recordObject.optString("anjianbh", "N/A"));
                            recordEntity.setRid(recordObject.optString("rid", "N/A"));
                            recordEntity.setZhuanlimc(recordObject.optString("zhuanlimc", "N/A"));
                            recordEntity.setShoujianrid(recordObject.optString("shoujianrid", "N/A"));
                            recordEntity.setZhuanlisqh(recordObject.optString("zhuanlisqh", "N/A"));
                            recordEntity.setTongzhismc(recordObject.optString("tongzhismc", "N/A"));
                            recordEntity.setDianzifwrq(recordObject.optString("dianzifwrq", "N/A"));
                            recordEntity.setId(i);

                            String dianzisqfs = recordEntity.getApply_for();
                            if (dianzisqfs == null) {
                                recordEntity.setApply_for("N/A");
                            } else {
                                switch (dianzisqfs) {
                                    case CrawlerTaskConstant.ONLINE_FILING_CODE:
                                        recordEntity.setDianzisqfs(CrawlerTaskConstant.ONLINE_FILING);
                                        break;
                                    case CrawlerTaskConstant.CLIENT_ELECTRONIC_FILING_CODE:
                                        recordEntity.setDianzisqfs(CrawlerTaskConstant.CLIENT_ELECTRONIC_FILING);
                                        break;
                                    default:
                                        recordEntity.setDianzisqfs("N/A");
                                }
                            }

                            crawlerTaskMapper.saveRecord(recordEntity);

                            //打印保存的记录信息
                            log.info("专利号：{} 已存入数据库", zhuanlisqh);
                            totalSaved++;
                            //打印已存入的数据条数
                            log.info("已存入 " + totalSaved + " 条数据");
                        }
                    }
                    //如果当前页没有更多数据则结束循环
                    if (recordsArray.length() < size) {
                        log.info("没有更多数据，爬取结束");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
