package com.chen.gmall.service;

import com.chen.gmall.bean.PmsBaseAttrInfo;
import com.chen.gmall.bean.PmsBaseAttrValue;

import java.util.List;
import java.util.Set;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);


    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet);
}
