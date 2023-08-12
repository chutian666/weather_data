package com.scboanda.demo.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ChuTian
 * @Title 预测数据Dao
 * @since 2022/4/30 16:42
 */
@Mapper
public interface YcsjDao {

    /**
     * 新增小时预测数据
     *@author ChuTian
     *@date 2022/4/30
     *@param list
     *@return
     */
    int addHourYcsj(List<Map<String, String>> list);


    /**
     * 删除小时预测数据
     *@author ChuTian
     *@date 2022/7/5
     *@param
     *@return
     */
    int deleteHourYcsj(String date);

    /**
     * 新增日预测数据
     *@author ChuTian
     *@date 2022/4/30
     *@param
     *@return
     */
    int addDayYcsj(List<Map<String, String>> list);

    /**
     * 删除日预测数据
     *@author ChuTian
     *@date 2022/7/5
     *@param
     *@return
     */
    int deleteDayYcsj(String date);



    /**
     * 查询小时预测数据
     *@author ChuTian
     *@date 2022/5/5
     *@param siteId 站点编号
     *@return
     */
    List<Map<String,Object>> queryHourYcsj(@Param("siteId")String siteId,@Param("startTime")String startTime,@Param("endTime")String endTime);

    /**
     * 查询日预测数据
     *@author ChuTian
     *@date 2022/5/5
     *@param siteId 站点编号
     *@return
     */
    List<Map<String,Object>> queryDayYcsj(@Param("siteId")String siteId,@Param("startTime")String startTime,@Param("endTime")String endTime);
}
