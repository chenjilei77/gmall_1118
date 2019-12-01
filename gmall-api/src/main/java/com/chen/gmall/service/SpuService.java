package com.chen.gmall.service;

import com.chen.gmall.bean.PmsBaseSaleAttr;
import com.chen.gmall.bean.PmsProductInfo;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsBaseSaleAttr> baseSaleAttrList();
}
