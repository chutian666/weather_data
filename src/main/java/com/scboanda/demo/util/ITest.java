package com.scboanda.demo.util;


import com.alibaba.fastjson.JSONArray;
import com.dahuatech.hutool.http.Method;
import com.dahuatech.hutool.json.JSONUtil;
import com.dahuatech.icc.exception.ClientException;
import com.dahuatech.icc.oauth.http.DefaultClient;
import com.dahuatech.icc.oauth.http.IClient;
import com.dahuatech.icc.oauth.http.IccHttpHttpRequest;
import com.dahuatech.icc.oauth.http.IccTokenResponse;
import com.dahuatech.icc.oauth.model.v202010.GeneralRequest;
import com.dahuatech.icc.oauth.model.v202010.GeneralResponse;
import com.dahuatech.icc.oauth.model.v202010.OauthPublicKeyResponse;
import com.dahuatech.icc.oauth.profile.IccProfile;
import com.dahuatech.icc.util.BeanUtil;
import com.dahuatech.icc.util.SignUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ChuTian
 * @Title 大华视频监控对接
 * @since 2023/7/26 14:47
 */
public class ITest {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ITest.class);

    public static void main(String[] args) throws ClientException, IOException {
        //subsystemDetail("1000022");
        Map<String,Object> map = subsystem();
        String url = realtime("1000022$4$0$0", "1", "flv");
        String publicKey = publicKeyTest();
        String accessToken = userPasswordAuthTest(publicKey);
        String newUrl = url + "?" + "token=" + accessToken;
    }

    /**
     * 用户密码模式-获取公钥
     * @throws ClientException
     */
    public static String publicKeyTest() throws ClientException {
        IClient iClient = new DefaultClient();
        log.info("----开始执行----{}------请求地址:{}", "用户密码模式-获取公钥");
        IccHttpHttpRequest pubRequest = new IccHttpHttpRequest(IccProfile.URL_SCHEME + "/evo-apigw/evo-oauth/1.0.0/oauth/public-key", Method.GET);
        String pubBody = pubRequest.execute();
        OauthPublicKeyResponse keyResp = (OauthPublicKeyResponse) BeanUtil.toBean(pubBody, OauthPublicKeyResponse.class);
        log.info("----结束执行----{}------返回报文:{}", "用户密码模式-获取公钥",keyResp);
        String publicKey = keyResp.getData().getPublicKey();
        return publicKey;
        // ----结束执行----用户密码模式-获取公钥------返回报文:OauthPublicKeyResponse{data=PublicKeyData{publicKey='MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCrNZqCdO1hupvYurhxj+kjz70466u0KdKWMJQN+Y7+NLurenYnVoSijTMKa6qODrpWyr6rI8j+USzYXwvgxZNshxVSFlX+SLd51wwW7DcoXG0mVtQ1YrlqzV44p+uW+ubmBHkYQtZxlUqA2bwnzA7UWg4J94gpMUE/SWac9Mpk8QIDAQAB'}}
    }

    /**
     * 用户密码模式-认证申请
     * @throws ClientException
     */
    public static String userPasswordAuthTest(String publicKey) throws ClientException {
        IClient iClient = new DefaultClient();
        log.info("----开始执行----{}------请求地址:{}", "用户密码模式-认证申请");
        Map<String, Object> map = new HashMap();
        map.put("grant_type", "password");
        map.put("username", IccProfile.username);
        map.put("password", SignUtil.encryptRSA(IccProfile.password,publicKey));
        map.put("client_id", IccProfile.pwdClientId);
        map.put("client_secret", IccProfile.pwdClientSecret);
        map.put("public_key", publicKey);
        IccHttpHttpRequest pr = new IccHttpHttpRequest(IccProfile.URL_SCHEME + "/evo-apigw/evo-oauth/1.0.0/oauth/extend/token", Method.POST, JSONUtil.toJsonStr(map));
        String prBody = pr.execute();
        IccTokenResponse keyResp = (IccTokenResponse)BeanUtil.toBean(prBody, IccTokenResponse.class);
        log.info("----结束执行----{}------返回报文:{}", "用户密码模式-认证申请",keyResp);
        String accessToken = keyResp.getData().getAccess_token();
        return accessToken;
        //- ----结束执行----用户密码模式-认证申请------返回报文:IccTokenResponse{data=IccToken{access_token='1:b8374233-dbf1-4a58-a2fe-1c304287132d', token_type='bearer', refresh_token='f5dd69a1-2a4b-4538-9c21-b0e95eb1a5bf', expires_in=7199, scope='supplier', userId='1', magicId='fb5a1be59e0b-12c9-8354-b4a2-1a96dd5f', remainderDays=80, ttl=null}}
    }

    /**
     * 实时预览 “HLS、FLV、RTMP实时拉流”
     *
     * @param channelCode   通道编码
     * @param streamType    码流类型：1=主码流，2=辅码流
     * @param type  拉流方式：hls,flv,rtmp
     * @return  流地址
     * @throws ClientException  客户端异常
     */
    public static String realtime(String channelCode, String streamType, String type) throws ClientException, IOException {
        IClient iClient = new DefaultClient();
        IccHttpHttpRequest realtimeRequest = new IccHttpHttpRequest(IccProfile.URL_SCHEME + "/evo-apigw/admin/API/video/stream/realtime", Method.POST);
        // 参数注释：
        //  channelId 视频通道编码
        //  streamType 码流类型：1=主码流，2=辅码流
        //  type 拉流方式：hls,flv,rtmp
        String realtimeBody = "{\"data\":{\"channelId\":\"%s\",\"streamType\":\"%s\",\"type\":\"%s\"}}";
        realtimeBody = String.format(realtimeBody, channelCode, streamType, type);
        log.info("请求参数：{}", realtimeBody);
        realtimeRequest.body(realtimeBody);
        String realtimeResponse = iClient.doAction(realtimeRequest);
        String url = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readValue(realtimeResponse, JsonNode.class).get("data");
            url = data.get("url").asText();
        } catch (JsonProcessingException e) {
            log.error("realtimeResponse[{}] format error", realtimeResponse, e);
            throw new RuntimeException("response format error");
        }
        return url;
    }

    /**
     * 获取设备列表
     * @throws ClientException
     */
    public static Map<String,Object> subsystem() throws ClientException {
        IClient iClient = new DefaultClient();
        /**
         * 1、请求地址是统一网关入口，以 /evo-apigw 开头
         * 2、方法参见 @see com.dahuatech.hutool.http.Method
         */
        IccHttpHttpRequest generalRequest = new IccHttpHttpRequest(IccProfile.URL_SCHEME + "/evo-apigw/evo-brm/1.2.0/device/subsystem/page", Method.POST);
        // 设置参数
        Map<String, Object> param = new HashMap<>();
        param.put("pageSize", 100);
        generalRequest.body(JSONUtil.toJsonStr(param));
        // 发起请求处理应答
        String response = iClient.doAction(generalRequest);
        Map<String, Object> map = JSONArray.parseObject(response);
        return map;
    }

    /**
     * 获取设备详情
     *
     * @param deviceCode   设备编码
     * @return  流地址
     * @throws ClientException  客户端异常
     */
    public static String subsystemDetail(String deviceCode) throws ClientException, IOException {
        IClient iClient = new DefaultClient();
        IccHttpHttpRequest realtimeRequest = new IccHttpHttpRequest(IccProfile.URL_SCHEME + "/evo-apigw/evo-brm/1.0.0/device/" + deviceCode, Method.GET);
        String realtimeResponse = iClient.doAction(realtimeRequest);
        String url = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readValue(realtimeResponse, JsonNode.class).get("data");
            url = data.get("_").asText();
        } catch (JsonProcessingException e) {
            log.error("realtimeResponse[{}] format error", realtimeResponse, e);
            throw new RuntimeException("response format error");
        }
        return url;
    }
}
