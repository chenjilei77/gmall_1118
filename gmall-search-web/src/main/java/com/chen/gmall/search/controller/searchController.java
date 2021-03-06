package com.chen.gmall.search.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.chen.gmall.annotations.LoginRequired;
import com.chen.gmall.bean.*;
import com.chen.gmall.service.AttrService;
import com.chen.gmall.service.SearchService;
import com.chen.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;


@Controller
public class searchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){

        //调用搜索服务，返回搜索结guo


        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);

        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);


        //抽取检索结果锁包含的平台属性集合

        Set<String> valueIdSet = new HashSet<>();

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList1 = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList1) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        //根据valueId将属性列表查询出来

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);

        modelMap.put("attrList",pmsBaseAttrInfos);


        //对平台属性进一步处理，去掉当前条件中valueId所在的属性组

        String[] delValueIds = pmsSearchParam.getValueId();

        if (delValueIds != null) {

            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String delValueId : delValueIds) {

                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam,delValueId));

                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();

                        if (delValueId.equals(valueId)) {

                            //查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }

                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }

            modelMap.put("attrValueSelectedList",pmsSearchCrumbs);

        }

        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);

        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",keyword);
        }


        return "list";
    }

    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam,String delValueId) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if(StringUtils.isNotBlank(keyword)){

            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;
        }

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }

        if(skuAttrValueList!=null){
            for (String  pmsSkuAttrValue : skuAttrValueList) {
                if(!pmsSkuAttrValue.equals(delValueId)){
                    urlParam = urlParam +"&valueId="+pmsSkuAttrValue;
                }
            }
        }

        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if(StringUtils.isNotBlank(keyword)){

            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;
        }

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }

        if(skuAttrValueList!=null){
            for (String  pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam +"&valueId="+pmsSkuAttrValue;
            }
        }

        return urlParam;
    }


    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){


        return null;
    }

}
