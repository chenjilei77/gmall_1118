package com.chen.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chen.gmall.bean.PmsProductSaleAttr;
import com.chen.gmall.bean.PmsSkuInfo;
import com.chen.gmall.bean.PmsSkuSaleAttrValue;
import com.chen.gmall.service.SkuService;
import com.chen.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService suSerpvice;

    @RequestMapping("{skuId}.html")
    public  String  item(@PathVariable String skuId,ModelMap map){

        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        //sku对象
        map.put("skuInfo",pmsSkuInfo);


        List<PmsProductSaleAttr> pmsProductSaleAttrList = suSerpvice.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),skuId);
        //sku销售属性
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrList);

        //查询当前的sku的其他sku的集合的哈希表

        Map<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());

        for (PmsSkuInfo skuInfo : pmsSkuInfos) {

            String k ="";
            String v = skuInfo.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";

            }
            skuSaleAttrHash.put(k,v);

        }
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
        map.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);

        return "item";
    }
}
