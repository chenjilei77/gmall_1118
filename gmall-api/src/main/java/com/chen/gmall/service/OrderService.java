package com.chen.gmall.service;

import com.chen.gmall.bean.OmsOrder;

import java.math.BigDecimal;

public interface OrderService {
    String checkTradeCode(String memberId,String tradeCode);

    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);
}
