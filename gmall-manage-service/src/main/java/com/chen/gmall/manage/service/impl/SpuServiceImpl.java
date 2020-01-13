package com.chen.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chen.gmall.bean.*;
import com.chen.gmall.manage.mapper.*;
import com.chen.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;

    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;


    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {

        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = pmsBaseSaleAttrMapper.selectAll();
        return pmsBaseSaleAttrs;
    }

    @Override
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        //保存商品信息
        pmsProductInfoMapper.insertSelective(pmsProductInfo);

        //生成商品主键
        String productId = pmsProductInfo.getId();

        //保存图片商品信息
        List<PmsProductImage> spuImageList =  pmsProductInfo.getSpuImageList();
        for (PmsProductImage pmsProductImage:spuImageList) {

            pmsProductImage.setProductId(productId);
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }

        //保存销售属性信息
        List<PmsProductSaleAttr> spuSaleAttrList =  pmsProductInfo.getSpuSaleAttrList();
        for(PmsProductSaleAttr pmsProductSaleAttr:spuSaleAttrList){
            pmsProductSaleAttr.setProductId(productId);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);

            //保存销售属性值
            List<PmsProductSaleAttrValue>  spuSaleAttrValueList =  pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue:spuSaleAttrValueList) {

                pmsProductSaleAttrValue.setProductId(productId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);

            }

        }


    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {

        PmsProductSaleAttr productSaleAttr = new     PmsProductSaleAttr();
        productSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductSaleAttrMapper.select(productSaleAttr);

        for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductSaleAttrList) {

            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList= pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            pmsProductSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValueList);
        }

        return pmsProductSaleAttrList;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage =  new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {

//        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
//        pmsProductSaleAttr.setProductId(productId);
//        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
//        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
//            String saleAttrId = productSaleAttr.getSaleAttrId();
//
//            PmsProductSaleAttrValue pmsProductSaleAttrValue =  new PmsProductSaleAttrValue();
//            pmsProductSaleAttrValue.setProductId(productId);
//            pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
//            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
//            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
//        }

        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);
        return pmsProductSaleAttrs;
    }
}
