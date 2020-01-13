package com.chen.gmall.manage.util;


import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {
    public static String uploadImag(MultipartFile multipartFile) {
        String imgUrl = "http://192.168.48.128";

        //上传图片到服务器
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getFile();//获得配置文件的路径

        try {
            ClientGlobal.init(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageClient storageClient = new StorageClient(trackerServer,null);

        try {

            byte[] bytes = multipartFile.getBytes();
             String originalFilename= multipartFile.getOriginalFilename();
            int i= originalFilename.lastIndexOf(".");
            String extName= originalFilename.substring(i+1);
            String[] uploadInfos = storageClient.upload_file(bytes,extName,null);

            for (String uploadInfo:uploadInfos) {

                imgUrl += "/" + uploadInfo;

            }

        } catch (   Exception e) {
            e.printStackTrace();
        }


        return imgUrl ;
    }
}
