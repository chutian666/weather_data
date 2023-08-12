package com.scboanda.demo.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ChuTian
 * @Title
 * @since 2023/2/24 17:04
 */
public class Test {

    public static void main(String[] args) {
        //推送url
        String urlTop = "http://localhost:8080/wcsst/CollectionData/collectionAutomaticStationData";
        //参数
        String paramStr = "{'data':[{'equipmentId': '1675220641063009867265','dataTime': '1675771200','factor': [{'avg': 4.2,'name': 'PH','code': 'w01001','isExceeding': 1,'standardValue': 7.0,'waterQuality': 1}]}]}";
        Map<String,Object> param = JSONObject.parseObject(paramStr);
        //调用接口获取返回值
        String result = sendPost(urlTop, param);
        Map<String,Object> resultMap = JSONObject.parseObject(result, HashMap.class);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @param params 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, final Map<String, Object> params) {
        CloseableHttpClient httpClient;
        HttpPost httpPost;
        String result = null;
        CloseableHttpResponse response;
        HttpEntity entity;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try{
            //第一步：创建HttpClient对象
            httpClient = HttpClients.createDefault();
            //第二步：创建httpPost对象
            httpPost = new HttpPost(url);
            //map转json
            String json = JSONObject.toJSONString(params);
            //第四步：给httpPost设置JSON格式的参数
            StringEntity requestEntity = new StringEntity(json,"UTF-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);
            //第五步：发送HttpPost请求，获取返回值
            response = httpClient.execute(httpPost);
            //调接口获取返回值时，必须用此方法
            /*result = httpClient.execute(httpPost,responseHandler);*/

            entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }
}
