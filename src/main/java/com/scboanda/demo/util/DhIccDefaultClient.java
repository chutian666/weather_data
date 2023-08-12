package com.scboanda.demo.util;

import com.dahuatech.icc.exception.ClientException;
import com.dahuatech.icc.oauth.http.DefaultClient;
import com.dahuatech.icc.oauth.http.IClient;
import org.springframework.context.annotation.Bean;

/**
 * @author ChuTian
 * @Title
 * @since 2023/7/26 14:44
 */
public class DhIccDefaultClient {

    @Bean
    public IClient iccDefaultClient() throws ClientException {
        return new DefaultClient();
    }
}
