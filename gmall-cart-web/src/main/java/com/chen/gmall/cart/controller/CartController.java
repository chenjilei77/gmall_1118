package com.chen.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chen.gmall.annotations.LoginRequired;
import com.chen.gmall.bean.OmsCartItem;
import com.chen.gmall.bean.PmsSkuInfo;
import com.chen.gmall.service.CartService;
import com.chen.gmall.service.SkuService;
import com.chen.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess =false)
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //调用服务修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductId(skuId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setMemberId(memberId);
        cartService.checkCart(omsCartItem);

        //将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        modelMap.put("cartList",omsCartItems);


        BigDecimal totalAmount = getTotalAmount(omsCartItems);

        modelMap.put("totalAmount",totalAmount);

        return "cartListInner";
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccess =false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if(StringUtils.isNotBlank(memberId)){
            //已经登陆，查询db
            omsCartItems = cartService.cartList(memberId);

        }else{

            //没有登陆,查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }

        }

        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }

        modelMap.put("cartList",omsCartItems);

        BigDecimal totalAmount = getTotalAmount(omsCartItems);

        modelMap.put("totalAmount",totalAmount);

        return "cartList";
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


    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess =false)
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response){

        List<OmsCartItem> omsCartItemList = new ArrayList<>();

        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("12113123123123123");
        omsCartItem.setQuantity(new BigDecimal(quantity));
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());

        //判断商品是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if(StringUtils.isBlank(memberId)){
            //用户没有登陆

            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if(StringUtils.isBlank(cartListCookie)){
                omsCartItemList.add(omsCartItem);
            }else{
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);

                //添加判断的购物车数据在我们的cookie中是否存在
                boolean exist = if_cart_exist(omsCartItemList,omsCartItem);

                if(exist){
                    //如果存在，说明之前添加过cookie，则更新购物车添加数量
                    for (OmsCartItem cartItem : omsCartItemList) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }

                }else{
                    //之前没有添加过，新增当前的购物车
                    omsCartItemList.add(omsCartItem);
                }

            }

            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItemList),60*60*72,true);

        }else{
            //用户已经登陆成功


            //从db中查出购物车数据
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId,skuId);

            if(omsCartItemFromDb==null){
                //该用户没有添加过当前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setQuantity(new BigDecimal(quantity));
                omsCartItem.setMemberNickname("test");

                cartService.addCart(omsCartItem);
            }else{
                //该用户添加过当前商品
                BigDecimal s1 = omsCartItemFromDb.getQuantity();
                BigDecimal s2 = omsCartItem.getQuantity();
                BigDecimal s3 = omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity());
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }

            //同步缓存
            cartService.flushCartCache(memberId);
        }


        return "redirect:/success.html";
    }

    private boolean if_cart_exist(List<OmsCartItem> omsCartItemList, OmsCartItem omsCartItem) {

        boolean exist = false;

        for (OmsCartItem cartItem : omsCartItemList) {

            String productSkuId = cartItem.getProductSkuId();

            if(productSkuId.equals(omsCartItem.getProductSkuId()))
                exist = true;
        }

        return exist;
    }
}
