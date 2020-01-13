package com.chen.gmall.service;

import com.chen.gmall.bean.PmsBaseSaleAttr;
import com.chen.gmall.bean.PmsProductImage;
import com.chen.gmall.bean.PmsProductInfo;
import com.chen.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);
}
