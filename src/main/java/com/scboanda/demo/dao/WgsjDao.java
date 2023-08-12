package com.scboanda.demo.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author ChuTian
 * @since 2022/2/16 17:47
 */
@Mapper
public interface WgsjDao {

    /**
     * 新增网格数据日志
     *@author ChuTian
     *@date 2022/3/29
     *@param
     *@return
     */
    int insertWgsj(Map<String,Object> map);

    /**
     * 查询网格数据日志
     *@author ChuTian
     *@date 2022/4/30
     *@param
     *@return
     */
    List<Map<String,Object>> findWgsj(String id);


    /**
     * 查询站点信息
     *@author ChuTian
     *@date 2022/3/29
     *@param
     *@return
     */
    List<Map<String,Object>> getZd();

    /**
     * 删除下载失败的网格数据日志
     *@author ChuTian
     *@date 2023/5/20
     *@param
     *@return
     */
    void deleleWgsj();
}
