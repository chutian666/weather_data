/******************************************************************************
 * Copyright (C) ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳市博安达信息技术股份有限公司开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package com.scboanda.demo.util;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * HTTP请求工具类
 *
 * @author 赵松
 * @company 深圳市博安达信息技术股份有限公司
 * @since 2018/11/22
 */
public class HttpUtils {

    /** 日志对象*/
    protected static Logger logger = Logger.getLogger(HttpUtils.class);

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @param params 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, final Map<String, Object> params) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        String charset = "UTF-8";
        try{
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            //设置参数
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = params.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String,String> elem = (Map.Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
            }
            if(list.size() > 0){
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
                httpPost.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        }catch(Exception ex){
            //ex.printStackTrace();
            //请求数据异常，未请求到有效数据
            //logger.error(ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * 向指定 URL 发送Get方法的请求
     *
     * @param url 发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String userId, String pwd) {
        CloseableHttpClient httpClient;
        HttpGet httpGet;
        String result = null;
        try {
            httpClient = HttpClients.createDefault();
            httpGet = new HttpGet(url);
            //添加key到请求头
            httpGet.setHeader("Authorization", "Basic " + encode(userId + ":" + pwd));
            HttpResponse response = httpClient.execute(httpGet);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "UTF-8");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //请求数据异常，未请求到有效数据
        }
        return result;
    }

    /**
     * 给字符串加密
     *
     * @param text
     * @return
     */
    private static String encode(String text) {
        byte[] textByte = new byte[0];
        try {
            textByte = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(textByte);
    }
}
