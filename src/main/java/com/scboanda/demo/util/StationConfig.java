package com.scboanda.demo.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author ChuTian
 * @Title 站点配置文件类
 * @since 2022/4/30 16:02
 */
@Configuration
@PropertySource("classpath:config/station.properties")
@Component
@Data
public class StationConfig {

    @Value("${yc.zdbh}")
    private String stationId;

}
