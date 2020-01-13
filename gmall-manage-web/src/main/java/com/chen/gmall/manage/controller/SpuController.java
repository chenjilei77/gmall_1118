package com.chen.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chen.gmall.bean.PmsBaseSaleAttr;
import com.chen.gmall.bean.PmsProductImage;
import com.chen.gmall.bean.PmsProductInfo;
import com.chen.gmall.bean.PmsProductSaleAttr;
import com.chen.gmall.manage.util.PmsUploadUtil;
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


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);

        return pmsProductImages;

    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> pmsProductSaleAttrList = spuService.spuSaleAttrList(spuId);

        return pmsProductSaleAttrList;

    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        //将图片或者音视频上传至分布式图片存储系统中



        //图片的存储路径返回给页面
        String imgUrl= PmsUploadUtil.uploadImag(multipartFile);

        System.out.println(imgUrl);

        return imgUrl;

    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

        spuService.saveSpuInfo(pmsProductInfo);

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
