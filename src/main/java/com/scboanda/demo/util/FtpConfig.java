/******************************************************************************
 * Copyright (C) ShenZhen Powerdata Information Technology Co.,Ltd All Rights Reserved.
 * 本软件为深圳市博安达信息技术股份有限公司开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、 复制、修改或发布本软件.
 *****************************************************************************/
package com.scboanda.demo.util;

/**
 * @title:
 * @fileName: FtpConfig.java
 * @description:
 * @copyright: PowerData Software Co.,Ltd. Rights Reserved.
 * @company: 深圳市博安达信息技术股份有限公司
 * @author： 谢维龙 @date： 2020/4/14 11:23
 * @version： V1.0
 */
public class FtpConfig {
    public String ip;
    public int port;
    public String userName;
    public String password;

    public FtpConfig setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public FtpConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public FtpConfig setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public FtpConfig setPassword(String password) {
        this.password = password;
        return this;
    }
}
