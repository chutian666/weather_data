package com.scboanda.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.scboanda.demo.dao.WgsjDao;
import com.scboanda.demo.dao.YcsjDao;
import com.scboanda.demo.service.WeatherService;
import com.scboanda.demo.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chut
 * @since 2022/1/18 11:42
 */
@Service("WeatherService")
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private YcsjDao ycsjDao;

    @Override
    public JSONObject client(String url, HttpMethod method, LinkedMultiValueMap params) {
        RestTemplate template = new RestTemplate();
        ResponseEntity<JSONObject> response = template.getForEntity(url, JSONObject.class);
        return response.getBody();
    }

    @Override
    public List<Map<String, Object>> getForecastDataHour(String siteId,String startTime,String endTime) {
        return ycsjDao.queryHourYcsj(siteId,startTime,endTime);
    }

    @Override
    public List<Map<String, Object>> getForecastDataDay(String siteId, String startTime, String endTime) {
        return ycsjDao.queryDayYcsj(siteId,startTime,endTime);
    }


}
