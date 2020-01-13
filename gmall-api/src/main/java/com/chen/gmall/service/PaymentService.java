package com.chen.gmall.service;

import com.chen.gmall.bean.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);
}
