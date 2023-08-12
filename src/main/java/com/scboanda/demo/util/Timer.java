package com.scboanda.demo.util;


import com.alibaba.fastjson.JSONObject;
import com.scboanda.demo.dao.WgsjDao;
import com.scboanda.demo.dao.YcsjDao;
import com.scboanda.demo.service.WeatherService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.joda.time.DateTime;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定时任务
 *
 * @author Chut
 * @since 2022/1/18 14:34
 */

@Component
@Lazy(false)
public class Timer {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WgsjDao wgsjDao;

    @Autowired
    private YcsjDao ycsjDao;

    @Autowired
    private StationConfig stationConfig;

    private static final Logger logger = LoggerFactory.getLogger(Timer.class);

    /**
     * 令牌
     */
    private static String token = "ejJPPGpyxBgeDEdv";

    /**
     * 网格数据采集标识 主服务器:main，副服务器:secondary
     */
    private static String wgsjFlag = "secondary";

    /**
     * 运行服务器标识 windows,linux
     */
    private static String systemFlag = "windows";

    /**
     * 接口测试
     *
     * @param
     * @return
     * @author ChuTian
     * @date 2022/2/16
     */
    //@Scheduled(cron = "0 0 * * *  ?")
    public void executeTimer() {
        String urlTop = "https://api.caiyunapp.com/v2.5/";
        //经纬度map
        Map<String, String> coordinateMap = new HashMap<>();
        //武昌区坐标
        coordinateMap.put("武昌区", "114.31589,30.55389");
        //洪山区坐标
        coordinateMap.put("洪山区", "114.31589,30.55389");
        //江夏区坐标
        coordinateMap.put("江夏区", "114.31589,30.55389");
        //江汉区坐标
        coordinateMap.put("江汉区", "114.31589,30.55389");
        //汉阳区坐标
        coordinateMap.put("汉阳区", "114.31589,30.55389");
        for (Map.Entry<String, String> coordinate : coordinateMap.entrySet()) {
            //通用预报接口
            String weatherUrl = urlTop + token + "/" + coordinate.getValue() + "/weather.json";
            //实况天气接口
            String realtimeUrl = urlTop + token + "/" + coordinate.getValue() + "/realtime.json";
            //分钟级降雨预报接口
            String minutelyUrl = urlTop + token + "/" + coordinate.getValue() + "/minutely.json";
            //小时级预报接口
            String hourlyUrl = urlTop + token + "/" + coordinate.getValue() + "/hourly.json";
            //天气级预报接口
            String dailyUrl = urlTop + token + "/" + coordinate.getValue() + "/daily.json";
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put("通用预报", weatherUrl);
            urlMap.put("实况天气", realtimeUrl);
            urlMap.put("分钟级降雨预报", minutelyUrl);
            urlMap.put("小时级预报", hourlyUrl);
            urlMap.put("天气级预报", dailyUrl);
            //请求方式
            HttpMethod method = HttpMethod.GET;
            //返回体
            LinkedMultiValueMap params = new LinkedMultiValueMap();
            for (Map.Entry<String, String> url : urlMap.entrySet()) {
                JSONObject urlModel = weatherService.client(url.getValue(), method, params);
                System.out.printf(new Date() + url.getKey() + coordinate.getKey() + coordinate.getValue() + urlModel.toJSONString());
                Map<String, Object> dataMap = new HashMap<>();
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日HH时");
                String date = format.format(new Date());
                //存入时间
                dataMap.put("date", date);
                //接口数据类型
                dataMap.put("url", url.getKey() + url.getValue());
                //存入点位
                dataMap.put("address", coordinate.getKey() + coordinate.getValue());
                //存入数据
                dataMap.put("data", urlModel.toJSONString());
                String templatePath = "D:\\template.docx";
                String fileDir = "D:\\weather\\" + date;
                String fileName = url.getKey() + coordinate.getKey();
                String wordPath = WordUtil.createWord(templatePath, fileDir, fileName, dataMap);
            }
        }
    }


    /**
     * 网格数据
     *
     * @param
     * @return
     * @author ChuTian
     * @date 2022/2/16
     */
    //@Scheduled(cron = "0 0 * * *  ?")
    //@Scheduled(cron = "*/15 * * * * ?")
    @Scheduled(cron = "0 45 0/1 * * ?")
    public void wgsj() throws ParseException {
        //删除无效下载记录
        wgsjDao.deleleWgsj();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String urlTop = "http://qd1-dev1.in.caiyunapp.com/cldas/raw?token=" + Timer.token + "&hours=24";
        HttpMethod method = HttpMethod.GET;
        //返回体
        LinkedMultiValueMap params = new LinkedMultiValueMap();
        JSONObject urlModel = weatherService.client(urlTop, method, params);
        Map<String, Object> dataMap = urlModel.getInnerMap();
        //返回的时间戳和file地址
        List<Map<String, Object>> listMap = (List<Map<String, Object>>) dataMap.get("data");
        //获取开始时间
        long startTime = System.currentTimeMillis();
        for (Map<String, Object> map : listMap) {
            String date = stampToTime(map.get("timestamp").toString());
            List<String> urlList = castList(map.get("files"), String.class);
            for (String url : urlList) {
                //获取文件名
                String str1 = url.substring(0, url.indexOf("?"));
                List<String> array = Arrays.asList(str1.split("_"));
                String str2 = array.get(array.size() - 1);
                //拼接日志id
                String id = str2 + "_" + date;
                List<Map<String, Object>> wgsjList = wgsjDao.findWgsj(id);
                if (wgsjList.isEmpty()) {
                    //开始拼接路径
                    List<String> array1 = Arrays.asList(str2.split("-"));
                    String newDate = array1.get(array1.size() - 1);
                    String newDate1 = newDate.substring(0, 4) + "-" + newDate.substring(4, 6) + "-" + newDate.substring(6, 8) + " " + newDate.substring(8, 10) + ":00:00";
                    Date dateTime = sdf1.parse(newDate1);
                    //文件名时间为UTC时间，需要加8小时改为东八区时间
                    Date newDateTime = addDateHours(dateTime, 8);
                    String newDateTimeStr = (sdf.format(newDateTime) != null) ? sdf.format(newDateTime) : "";
                    String filePath;
                    if (systemFlag.equals("windows")) {
                        filePath = "D:\\data\\caiyun\\nc\\" + newDateTimeStr;
                    } else {
                        filePath = "/bowo/data/caiyun/nc/" + newDateTimeStr;
                    }
                    //如果是日数据则存放在日数据文件夹
                    if (str2.indexOf("DAY") >= 0) {
                        if (systemFlag.equals("windows")) {
                            filePath = "D:\\data\\caiyun\\nc\\" + newDateTimeStr.substring(0, 8);
                        } else {
                            filePath = "/bowo/data/caiyun/nc/" + newDateTimeStr.substring(0, 8);
                        }
                    }
                    String strFile = filePath + "\\" + str2;
                    File file = new File(strFile);
                    if (!file.exists()) {
                        System.out.println("创建文件路径" + file);
                        saveUrlAs(url, filePath, str2, "GET");
                        Map<String, Object> logMap = new HashMap<>();
                        logMap.put("id", id);
                        logMap.put("fileName", str2);
                        logMap.put("url", url);
                        logMap.put("createTime", DateToString(new Date()));
                        logMap.put("remarks", "主服务器存储:" + strFile);
                        logMap.put("fileDateTime", newDate1);
                        logMap.put("newFileDateTime", newDateTimeStr);
                        if ("main".equals(wgsjFlag)) {
                            wgsjDao.insertWgsj(logMap);
                        } else {
                            //同步文件到共享文件夹
                            try {
                                System.out.println("开始同步文件");
                                //共享文件夹路径
                                String remoteUrl;
                                if (str2.indexOf("DAY") >= 0) {
                                    remoteUrl = "smb://192.168.3.217/Data/caiyun/nc/" + newDateTimeStr.substring(0, 8);
                                } else {
                                    remoteUrl = "smb://192.168.3.217/Data/caiyun/nc/" + newDateTimeStr;
                                }
                                String userName = "Administrator";
                                String pwd = "bovosz@217";
                                WindowsUploadUtil upload = new WindowsUploadUtil();
                                //这里要注意，remoteUrl参数（也就是第一个参数），必须是有smb://前缀的，这是协议！后面拼接ip地址，再拼接的就是第一步中，共享文件夹的共享名！
                                upload.smbPut(remoteUrl, strFile, userName, pwd);
                                System.out.println("完成同步=====>" + remoteUrl + str2);
                                //添加日志
                                logMap.put("remarks", "233服务器同步数据;" + strFile);
                                wgsjDao.insertWgsj(logMap);
                                System.out.println("写入文件" + str2 + "成功");
                                //删除本地文件
                                deleteFile(strFile);
                            } catch (Exception e) {
                                System.out.println("e:" + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("文件路径" + file + "已存在");
                    }
                } else {
                    System.out.println("文件" + str2 + "已存在，开始解析下一个文件");
                }
            }
        }
        //获取结束时间
        long endTime = System.currentTimeMillis();
        System.out.println(sdf1.format(new Date())
                + "网格数据下载任务执行完毕，程序运行时间： " + (endTime - startTime) + "ms");
    }

//    public static void main(String[] args) throws Exception {
//        WindowsUploadUtil Test = new WindowsUploadUtil();
//        //这里要注意，remoteUrl参数（也就是第一个参数），必须是有smb://前缀的，这是协议！后面拼接ip地址，再拼接的就是第一步中，共享文件夹的共享名！
//        Test.smbPut("smb://192.168.3.217/Data/Test/1111",
//                "D:/data/caiyun/nc/HOR-DPT-2022082901.jpg",
//                "Administrator","bovosz@217");
//    }


    //@Scheduled(cron = "0 0 * * *  ?")
    //@Scheduled(cron ="*/15 * * * * ?")
    /*public void wgsj() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HttpMethod method = HttpMethod.GET;
        //返回的时间戳和file地址
        String txt = txt2String(new File("D:/cldas_dumps.txt"));
        List<String> txtList = Arrays.asList(txt.split(","));
        int a = 0;
            for (String url : txtList) {
                a++;
                if (a <= 1) {
                    continue;
                }
                String str1 = url.substring(0, url.indexOf("?"));
                String[] array = str1.split("_");
                String str2 = null;
                for (String str : array) {
                    str2 = str;
                }
                String newDate = str2.substring(str2.indexOf("20"), str2.indexOf("20") + 10);
                String newDate1 = newDate.substring(0, 4) + "-" + newDate.substring(4, 6) + "-" + newDate.substring(6, 8) + " " + newDate.substring(8, 10) + ":00:00";
                Date dateTime = sdf1.parse(newDate1);
                Date newDateTime = addDateHours(dateTime, 8);
                String filePath = "D:\\data\\caiyun\\nc\\" + sdf.format(newDateTime);
                File file = new File(filePath + "\\" + str2);
                System.out.println("生成路径"+file);
                if (!file.exists()) {
                    System.out.println("文件路径"+file+"不存在，开始创建");
                    saveUrlAs(url, filePath, str2, "GET");
                    System.out.println("写入文件"+str2+"成功");
                }
            }
    }*/

    /**
     * 读取txt文件的内容
     *
     * @param file 想要读取的文件对象
     * @return 返回文件内容
     */
    public static String txt2String(File file) {
        StringBuilder result = new StringBuilder();
        try {
            //构造一个BufferedReader类来读取文件
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            //使用readLine方法，一次读一行
            while ((s = br.readLine()) != null) {
                result.append(System.lineSeparator() + s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 预测数据(每天1点执行)
     *
     * @param
     * @return
     * @author ChuTian
     * @date 2022/3/29
     */
    //@Scheduled(cron = "*/15 * * * * ?")
    @Scheduled(cron = "0 0 1 * * ?")
    public void ycsj() {
        if ("main".equals(wgsjFlag) && "windows".equals(systemFlag)) {
            List<Map<String, Object>> zdList = wgsjDao.getZd();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(new Date());
            //日期加减7天
            rightNow.add(Calendar.DAY_OF_YEAR, 1);
            //数据保留的最大时间，小于该时间的数据会删除
            String removeDate = sdf1.format(rightNow.getTime());
            //删除过期预测数据
            ycsjDao.deleteHourYcsj(removeDate);
            ycsjDao.deleteDayYcsj(removeDate);
            //预测数据存储路径
            String filePath = "D:\\data\\caiyun\\yc\\" + sdf.format(new Date()).substring(0, 6) + "\\" + sdf.format(new Date()) + ".xlsx";
            //小时数据
            List<Map<String, String>> hourData = new ArrayList<>();
            //日数据
            List<Map<String, String>> dayData = new ArrayList<>();
            for (Map<String, Object> zdMap : zdList) {
                String jd = MapUtils.getString(zdMap, "JD");
                String wd = MapUtils.getString(zdMap, "WD");
                //天数范围
                String dailySteps = "15";
                //小时范围
                String hourlySteps = "360";
                //站点id
                String zdId = MapUtils.getString(zdMap, "old_station_id");
                //站点名称
                String zdmc = MapUtils.getString(zdMap, "station_name");
                //访问链接
                String url = "https://api.caiyunapp.com/v2.5/" + token + "/" + jd + "," + wd + "/weather?dailysteps=" + dailySteps + "&hourlysteps=" + hourlySteps;
                RestTemplate template = new RestTemplate();
                ResponseEntity<JSONObject> response = template.getForEntity(url, JSONObject.class);
                JSONObject urlModel = response.getBody();
                //数据写入文件
                WordUtil.createJsonFile(urlModel, "D:\\data\\caiyun\\yc\\" + sdf.format(new Date()).substring(0, 6)
                        + "\\" + sdf.format(new Date()) + "\\" + zdId + ".txt");
                Map<String, Object> map = urlModel.getInnerMap();
                if (map != null) {
                    String date = stampToTime(MapUtils.getString(map, "server_time"));
                    Map<String, Object> result = (Map<String, Object>) map.get("result");
                    //小时预测数据
                    Map<String, Object> hourlyMap = (Map<String, Object>) result.get("hourly");
                    //日预测数据
                    Map<String, Object> dailyMap = (Map<String, Object>) result.get("daily");
                    //解析小时数据
                    List<Map<String, String>> hourlyData = dataLoad(hourlyMap, hourlySteps, zdId);
                    //hourData.addAll(hourlyData);
                    //解析日数据
                    List<Map<String, String>> dailyData = dataLoad(dailyMap, dailySteps, zdId);
                    //dayData.addAll(dailyData);
                    ycsjDao.addHourYcsj(hourlyData);
                    ycsjDao.addDayYcsj(dailyData);
                }
            }
        /*WordUtil.createExcel(hourData,filePath);
        WordUtil.createExcel(dayData,filePath);*/
        }
    }

    /**
     * 预测数据解析
     *
     * @param
     * @return
     * @author ChuTian
     * @date 2022/3/28
     */
    private static List<Map<String, String>> dataLoad(Map<String, Object> map, String num, String zdId) {
        int size = Integer.parseInt(num);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, String>> list = new ArrayList<>();
        //任务执行日期
        String zxrq = sdf1.format(new Date()).substring(0, 10);
        //循环小时次数
        for (int i = 0; i < size; i++) {
            Map<String, String> colMap = new HashMap<>();
            for (Map.Entry<String, Object> m : map.entrySet()) {
                String key = m.getKey();
                Object value = m.getValue();
                if (value instanceof List) {
                    List<Map<String, Object>> valueList = (List<Map<String, Object>>) value;
                    Map<String, Object> valueMap = valueList.get(i);
                    for (Map.Entry<String, Object> m1 : valueMap.entrySet()) {
                        if (m1.getValue() instanceof Map) {
                            Map<String, Object> listMap = (Map<String, Object>) m1.getValue();
                            for (Map.Entry<String, Object> m2 : listMap.entrySet()) {
                                colMap.put(key + "_" + m1.getKey() + "_" + m2.getKey(), m2.getValue().toString());
                            }
                        } else {
                            if ("date".equals(m1.getKey()) || "datetime".equals(m1.getKey())) {
                                String date = m1.getValue().toString();
                                colMap.put("stationId", zdId);
                                colMap.put("zxrq", zxrq);
                                colMap.put("cjsj", sdf1.format(new Date()));
                                colMap.put("ycsj", date.substring(0, 10) + " " + date.substring(11, 16));
                                colMap.put("id", zdId + "_" + sdf.format(new Date()) + "_" +
                                        date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10) + date.substring(11, 13));
                            }
                            colMap.put(key + "_" + m1.getKey(), m1.getValue().toString());
                        }
                    }
                } else if (value instanceof Map) {
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    for (Map.Entry<String, Object> mapValue : valueMap.entrySet()) {
                        List<Map<String, Object>> mapValueList = (List<Map<String, Object>>) mapValue.getValue();
                        Map<String, Object> mapValueListMap = mapValueList.get(i);
                        for (Map.Entry<String, Object> valueListMap : mapValueListMap.entrySet()) {
                            if (valueListMap.getValue() instanceof Map) {
                                Map<String, Object> valueListMapValue = (Map<String, Object>) valueListMap.getValue();
                                for (Map.Entry<String, Object> m1 : valueListMapValue.entrySet()) {
                                    colMap.put(key + "_" + mapValue.getKey() + "_" + valueListMap.getKey() + "_" + m1.getKey(), m1.getValue().toString());
                                }
                            } else {
                                colMap.put(key + "_" + mapValue.getKey() + "_" + valueListMap.getKey(), valueListMap.getValue().toString());
                            }
                        }
                    }
                }
            }
            list.add(colMap);
        }
        return list;
    }


    /**
     * 文件复制
     *
     * @param source 来源路径
     * @param dest   目标路径
     * @return
     * @author ChuTian
     * @date 2022/2/11
     */
    private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    /**
     * 读取csv文件
     *
     * @param path
     * @return List<HashMap < String, Object>>
     * @author ChuTian
     * @date 2022/2/10
     */
    public static List<Map<String, Object>> getCsvTableList(String path) {
        List<Map<String, Object>> retHashMap = new ArrayList<Map<String, Object>>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            // 第一行信息，为标题信息，不用，如果需要，注释掉
            String[] headTitle = reader.readLine().split(",");
            String line = null;
            while ((line = reader.readLine()) != null) {
                HashMap<String, Object> itemMap = new HashMap<String, Object>();
                String[] itemArray = line.split(",");
                for (int i = 0; i < itemArray.length; i++) {
                    itemMap.put(headTitle[i], itemArray[i]);
                }
                retHashMap.add(itemMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retHashMap;
    }

    /**
     * 时间戳转换和补正8小时
     *
     * @param data
     * @return String
     * @author ChuTian
     * @date 2022/2/10
     */
    public static String stampToTime(String data) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");
        Date newDate = new Date(Long.valueOf(data + "000"));
        //将时间戳转换为时间
        String sd = simpleDateFormat.format(newDate);
        return sd;
    }

    /**
     * Object转list
     *
     * @param
     * @return
     * @author ChuTian
     * @date 2022/2/16
     */
    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    /**
     * 删除单个文件
     *
     * @param fileName 文件名称
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    /**
     * 生成文件写入内容
     *
     * @param content  内容
     * @param filePath 路径
     * @return
     * @author ChuTian
     * @date 2022/2/10
     */
    public static void saveAsFileWriter(String content, String filePath) {
        FileWriter fwriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fwriter = new FileWriter(filePath, true);
            fwriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 下载材料接口
     *
     * @param filePath 文件将要保存的目录
     * @param method   请求方法，包括POST和GET
     * @param url      请求的路径
     * @return
     */
    public static void saveUrlAs(String url, String filePath, String fileName, String method) {
        FileOutputStream fileOut = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        //创建不同的文件夹目录
        File file = new File(filePath);
        //判断文件夹是否存在
        if (!file.exists()) {
            //如果文件夹不存在，则创建新的的文件夹
            file.mkdirs();
        }
        try {
            // 建立链接
            URL httpUrl = new URL(url);
            //获取开始时间
            long startTime = System.currentTimeMillis();
            System.out.println("建立链接");
            conn = (HttpURLConnection) httpUrl.openConnection();
            //以Post方式提交表单，默认get方式
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // post方式不能使用缓存
            conn.setUseCaches(false);
            //连接指定的资源
            conn.connect();
            //获取网络输入流
            inputStream = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            //判断文件的保存路径后面是否以/结尾
            if (!filePath.endsWith("/")) {
                filePath += "/";
            }
            //写入到文件（注意文件保存路径的后面一定要加上文件的名称）
            System.out.println("写入到文件" + filePath + fileName);
            fileOut = new FileOutputStream(filePath + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fileOut);

            byte[] buf = new byte[4096];
            int length = bis.read(buf);
            System.out.println("开始保存文件" + filePath + fileName);
            //保存文件
            while (length != -1) {
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }
            //获取结束时间
            long endTime = System.currentTimeMillis();
            System.out.println("写入完毕;总耗时" + (endTime - startTime) + "ms");
            bos.close();
            bis.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("抛出异常！！");
            logger.error(e.getMessage());
        }
    }


    /**
     * 日期格式转换字符串
     *
     * @param date
     * @return
     */
    public static String DateToString(Date date) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    /**
     * 对日期的【小时】进行加/减
     *
     * @param date  日期
     * @param hours 小时数，负数为减
     * @return 加/减几小时后的日期
     */
    protected static Date addDateHours(Date date, int hours) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusHours(hours).toDate();
    }


}
