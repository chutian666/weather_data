package com.scboanda.demo.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * @author Chut
 * @since 2022/1/18 11:41
 */
public interface WeatherService {

    /**
     * 接入数据
     *@author ChuTian
     *@date 2022/1/18
     *@param url
     *@param method
     *@param params
     *@return
     */
     JSONObject client(String url, HttpMethod method, LinkedMultiValueMap params);

     /**
      * 读取小时预测数据文件解析并返回
      *@author ChuTian
      *@date 2022/4/28
      *@param
      *@return
      */
     List<Map<String,Object>> getForecastDataHour(String siteId,String startTime,String endTime);

    /**
     * 读取日预时测数据文件解析并返回
     *@author ChuTian
     *@date 2022/4/28
     *@param
     *@return
     */
    List<Map<String,Object>> getForecastDataDay(String siteId,String startTime,String endTime);


}
