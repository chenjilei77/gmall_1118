package com.chen.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chen.gmall.bean.PmsBaseAttrInfo;
import com.chen.gmall.bean.PmsBaseAttrValue;
import com.chen.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.chen.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.chen.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {

            List<PmsBaseAttrValue> pmsBaseAttrValues = new ArrayList<>();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }


        return pmsBaseAttrInfos;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();
        if(StringUtils.isBlank(id)){
            //id为空，保存
            //保存属性
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);//insert和insertSelective后者不允许空值插入数据库

            //保存属性值
            List<PmsBaseAttrValue> attrValueList= pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);

            }
        }else{
            //id不为空，则为修改

            //属性
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

            //属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();

            //按照属性id，删除所有属性
            PmsBaseAttrValue pmsBaseAttrValueDel = new PmsBaseAttrValue();
            pmsBaseAttrValueDel.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValueDel);

            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);

            }
        }

        return "success";
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue  pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);

        List<PmsBaseAttrValue> pmsBaseAttrValueList = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

        return pmsBaseAttrValueList;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {

        String valueIdStr = StringUtils.join(valueIdSet, ",");

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);

        return pmsBaseAttrInfos;
    }

}
