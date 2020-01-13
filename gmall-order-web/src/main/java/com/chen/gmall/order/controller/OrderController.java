package com.chen.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chen.gmall.annotations.LoginRequired;
import com.chen.gmall.bean.*;
import com.chen.gmall.service.CartService;
import com.chen.gmall.service.OrderService;
import com.chen.gmall.service.SkuService;
import com.chen.gmall.service.UserService;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;


    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, String tradeCode, BigDecimal totalAmount, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //检查交易码
        String success = orderService.checkTradeCode(memberId,tradeCode);

        String outTradeNo = "gmall";
        outTradeNo = outTradeNo + System.currentTimeMillis();//将毫秒时间戳拼接
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
        outTradeNo =outTradeNo+ sdf.format(new Date());

        if(success.equals("success")){

            List<OmsOrderItem> omsOrderItems = new ArrayList<>();

            OmsOrder omsOrder = new OmsOrder();

            //根据用户id获得 商品列表和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

            for (OmsCartItem omsCartItem : omsCartItems) {
                if(omsCartItem.getIsChecked().equals("1")){
                    //获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem();


                    //验价
                    boolean b =  skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if(b==false){
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }

                    //验库存,远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());


                    omsOrderItem.setOrderSn(outTradeNo);//订单单号
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());

                    omsOrderItems.add(omsOrderItem);
                }
            }

            omsOrder.setOmsOrderItems(omsOrderItems);

            //将订单和订单详情写入数据库
            //删除购物车的对应商品
            orderService.saveOrder(omsOrder);


            //重定向到支付系统
            ModelAndView mv = new ModelAndView("redirecct:/http://payment.gmall.com8087/index");
            mv.addObject("outTradeNo",outTradeNo);
            mv.addObject("totalAmount",totalAmount);
            return mv;
        }else {
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }

    }

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //收件人地址列表
        List<UmsMemberReceiveAddress> userAddressList = userService.getReceiveAddressByMemberId(memberId);

        //将购物车集合转化为页面计算清单集合
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            //每循环一个购物车对象，就封装一个商品的详情到omsOrderItems

            if(omsCartItem.getIsChecked().equals("1")){
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItems.add(omsOrderItem);
            }
        }

        modelMap.put("omsOrderItems",omsOrderItems);

        modelMap.put("userAddressList",userAddressList);

        modelMap.put("totalAmount",getTotalAmount(omsCartItems));

        //生成交易码，为了在提交订单时做交易码的校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode",tradeCode);

        return "trade";
    }



    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {

        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }


        }

        return totalAmount;
    }
}
