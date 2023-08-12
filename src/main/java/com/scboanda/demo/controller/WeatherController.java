package com.scboanda.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.scboanda.demo.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Chut
 * @since 2022/1/18 11:48
 */
@RestController
@RequestMapping("weather")
public class WeatherController {

    @Autowired
    WeatherService weatherService;

    @RequestMapping("getData")
    public String getData(){
        String url = "https://api.caiyunapp.com/v2.5/SdL5rRK1nUa3IeGb/121.6544,25.1552/weather.json";
        HttpMethod method = HttpMethod.GET;
        LinkedMultiValueMap params = new LinkedMultiValueMap();
        JSONObject urlModel = weatherService.client(url, method, params);
        return urlModel.toJSONString();
    }

    /**
     * 查询小时预测数据
     *@author ChuTian
     *@date 2022/5/5
     *@param
     *@return
     */
    @RequestMapping("getForecastDataHour")
    public List<Map<String,Object>> getForecastDataHour(String siteId, String startTime, String endTime){
        return weatherService.getForecastDataHour(siteId,startTime,endTime);
    }

    /**
     * 查询日预测数据
     *@author ChuTian
     *@date 2022/5/5
     *@param
     *@return
     */
    @RequestMapping("getForecastDataDay")
    public List<Map<String,Object>> getForecastDataDay(String siteId, String startTime, String endTime){
        return weatherService.getForecastDataDay(siteId,startTime,endTime);
    }

}
