package com.chen.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chen.gmall.bean.PmsSkuAttrValue;
import com.chen.gmall.bean.PmsSkuImage;
import com.chen.gmall.bean.PmsSkuInfo;
import com.chen.gmall.bean.PmsSkuSaleAttrValue;
import com.chen.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.chen.gmall.manage.mapper.PmsSkuImageMapper;
import com.chen.gmall.manage.mapper.PmsSkuInfoMapper;
import com.chen.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.chen.gmall.service.SkuService;
import com.chen.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo){

        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();
        //插入平台属性关联
        List<PmsSkuAttrValue>  pmsSkuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //插入销售属性关联
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);

        }

        //插入图片信息
        List<PmsSkuImage> pmsSkuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : pmsSkuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);

        }

    }

    @Override
    public PmsSkuInfo getSkuByIdFromDb(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo= pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);

        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);

            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {

        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        BigDecimal price = pmsSkuInfo1.getPrice();

        if(price.compareTo(productPrice)==0){
            b = true;
        }

        return b;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        //链接缓存
        Jedis jedis = redisUtil.getJedis();

        //查询缓存
        String skuKey =  "sku:"+skuId+":info";

        String skuJson = jedis.get(skuKey);

        if(StringUtils.isNotBlank(skuJson)){

            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        }else{

            //如果缓存中没有再去查询mysql

            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10*1000);
            if(StringUtils.isNotBlank(OK) && OK.equals("OK")){

                //如果设置成功，有权在10秒的过期时间内访问数据库。
                pmsSkuInfo = getSkuByIdFromDb(skuId);

                //MySQL查询结果存入redis

                if(pmsSkuInfo != null){
                    jedis.set("sku:"+skuId+":info",JSON.toJSONString(pmsSkuInfo));
                }else{
                    //数据库中不存在该sku
                    //为了防止缓存穿透的情况，将null值设置给redis
                    jedis.setex("sku:"+skuId+":info",60*5,JSON.toJSONString(""));
                }

                //在访问MySQL数据库后，将MySQL的分布式锁释放
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                if(StringUtils.isNotBlank(lockToken) && lockToken.equals(token)){
                    jedis.del("sku:" + skuId + ":lock");
                }

            }else{
                //设置失败，自旋（该线程在睡眠几秒后，重新尝试访问本方法）
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);

            }

        }



        jedis.close();


        return pmsSkuInfo;
    }
}
