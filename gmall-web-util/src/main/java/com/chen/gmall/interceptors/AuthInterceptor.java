package com.chen.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.chen.gmall.annotations.LoginRequired;
import com.chen.gmall.util.CookieUtil;
import com.chen.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //拦截代码

        //判断被拦截的请求的访问的方法的注解（是否是需要拦截的）
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);


        //是否拦截
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        //是否必须登陆
        boolean loginSuccess = methodAnnotation.loginSuccess();//获得该请求是否必须获得登陆成功

        //调用认证中心进行验证
        String success ="fail";

        Map<String,String> successMap = new HashMap<>();
        if(StringUtils.isNotBlank(token)){

            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();//从request中获取IP

                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
           String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);

           successMap = JSON.parseObject(successJson, Map.class);

           success = successMap.get("status");

        }


        if (loginSuccess) {
            //必须登陆成功才能使用

            if (!success.equals("success")) {
                // 重定向回passport登陆
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="+requestURL);

                return false;

            } else {
                //验证登陆，覆盖Cookie中的token
                // 需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
            }


        } else {
            //不需要登陆也可以使用，但是必须验证
            if (success.equals("success")) {
                // 需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
            }
        }
        //覆盖Cookie中的token
        if(StringUtils.isNotBlank(token)){

            CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
        }

        return true;
    }
}
