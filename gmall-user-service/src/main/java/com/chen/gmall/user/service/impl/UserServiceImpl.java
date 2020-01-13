package com.chen.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chen.gmall.bean.UmsMember;
import com.chen.gmall.bean.UmsMemberReceiveAddress;
import com.chen.gmall.service.UserService;
import com.chen.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.chen.gmall.user.mapper.UserMapper;
import com.chen.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
        public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembersList = userMapper.selectAllUser();//userMapper.selectAllUser();
        return umsMembersList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        //封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis =  null;

        try {
            jedis = redisUtil.getJedis();

            if(jedis!=null) {
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword()+umsMember.getUsername()  + ":info");

                if (StringUtils.isNotBlank(umsMemberStr)) {
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //连接redis失败，开启数据库
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if (umsMemberFromDb != null) {
                jedis.setex("user:" + umsMember.getPassword()+umsMember.getUsername() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;

        }finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();


        jedis.setex("user:"+memberId+":token",60*60*2,token);


        jedis.close();
    }

    private UmsMember loginFromDb(UmsMember umsMember) {

        List<UmsMember> umsMembers = userMapper.select(umsMember);

        if(umsMembers!=null){
            return umsMembers.get(0);
        }
        return null;
    }
}
