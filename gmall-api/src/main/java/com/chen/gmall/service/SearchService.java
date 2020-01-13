package com.chen.gmall.service;

import com.chen.gmall.bean.PmsSearchParam;
import com.chen.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
