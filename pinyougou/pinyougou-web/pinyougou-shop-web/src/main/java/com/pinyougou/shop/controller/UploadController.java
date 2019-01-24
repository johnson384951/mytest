package com.pinyougou.shop.controller;

import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 上传控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-02<p>
 */
@RestController
public class UploadController {

    /** 注入文件服务器的访问地址 */
    @Value("${fileServerUrl}")
    private String fileServerUrl;


    /** 文件上传的方法 */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file")MultipartFile multipartFile){
        // 定义Map集合封装响应数据
        // {status : 200, url : ''}
        Map<String, Object> data = new HashMap<>();
        data.put("status", 500);
        try{
            // 1. 获取原文件名
            String originalFilename = multipartFile.getOriginalFilename();
            // 2. 获取上传文件的字节数组
            byte[] bytes = multipartFile.getBytes();

            // 3. 把文件上传到FastDFS图片服务器
            // 获取fastdfs-client.conf
            String path = this.getClass().getResource("/fastdfs-client.conf").getPath();
            // 初始化客户端全局对象
            ClientGlobal.init(path);
            // 创建存储客户端对象
            StorageClient storageClient = new StorageClient();
            // 调用上传的方法
            String[] arr = storageClient.upload_file(bytes,
                    FilenameUtils.getExtension(originalFilename), null);

            // http://192.168.12.131/ arr[0] / arr[1]
            // 定义StringBuilder拼接图片的访问URL
            StringBuilder sb = new StringBuilder(fileServerUrl);
            for (String str : arr){
                sb.append("/" + str);
            }

            data.put("status", 200);
            data.put("url", sb.toString());

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }
}
