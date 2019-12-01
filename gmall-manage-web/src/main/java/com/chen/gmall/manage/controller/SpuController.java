package com.chen.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chen.gmall.bean.PmsBaseSaleAttr;
import com.chen.gmall.bean.PmsProductInfo;
import com.chen.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        //将图片或者音视频上传至分布式图片存储系统中

        //图片的存储路径返回给页面
        String imgUrl="http://up.enterdesk.com/edpic/5e/55/e7/5e55e748a3943cdcdeca4bb8146733cc.jpg";
        return imgUrl;

    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){


        return "success";

    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;

    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = spuService.baseSaleAttrList();
        return pmsBaseSaleAttrs;

    }

}
