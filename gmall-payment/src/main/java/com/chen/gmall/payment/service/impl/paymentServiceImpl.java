package com.chen.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chen.gmall.bean.PaymentInfo;
import com.chen.gmall.payment.mapper.PaymentInfoMapper;
import com.chen.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

@Service
public class paymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        String orderSn = paymentInfo.getOrderSn();

        Example e = new Example(PaymentInfo.class);

        e.createCriteria().andEqualTo("orderSn",orderSn);

        paymentInfoMapper.updateByExampleSelective(paymentInfo,e);
    }
}
