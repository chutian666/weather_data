package com.scboanda.demo.util;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import java.io.*;

/**
 * 共享文件夹工具类
 * @author ChuTian
 * @Title
 * @since 2022/8/29 9:04
 */
public class WindowsUploadUtil {

    /**
     * Description: 从本地上传文件到共享目录
     * @param remoteUrl 共享文件目录
     * @param localFilePath 本地文件绝对路径
     */
    public void smbPut(String remoteUrl,String localFilePath, String userName, String pwd) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File(localFilePath);
            String fileName = localFile.getName();
            //这里推荐使用这种方式进行用户名密码的校验，在url中拼接，如果需要特殊字符可能无法转换
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, userName, pwd);
            //新建远程的文件夹,需要加上auth
            SmbFile remoteFolder = new SmbFile(remoteUrl,auth);
            //判断远程文件夹是否存在，如果不存在则创建
            if(!remoteFolder.exists()){
                remoteFolder.mkdirs();
            }
            SmbFile remoteFile = new SmbFile(remoteUrl + File.separator + fileName, auth);
            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            byte[] buffer = new byte[1024];
            while(in.read(buffer)!=-1){
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
