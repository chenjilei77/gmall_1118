package com.chen.gmall.user.service.impl;

import com.chen.gmall.bean.UmsMember;
import com.chen.gmall.bean.UmsMemberReceiveAddress;
import com.chen.gmall.service.UserService;
import com.chen.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.chen.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
        public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembersList = userMapper.selectAllUser();//userMapper.selectAllUser();
        return umsMembersList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        Example e  = new Example(UmsMemberReceiveAddress.class);
        umsMemberReceiveAddressMapper.selectByExample(e);
        return null;
    }
}
