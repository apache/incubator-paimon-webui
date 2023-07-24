/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.web.server.service.impl;

import org.apache.paimon.web.server.data.model.User;
import org.apache.paimon.web.server.data.result.exception.BaseException;
import org.apache.paimon.web.server.data.result.exception.user.UserNotExistsException;
import org.apache.paimon.web.server.data.result.exception.user.UserPasswordNotMatchException;
import org.apache.paimon.web.server.mapper.UserMapper;
import org.apache.paimon.web.server.service.UserService;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** UserServiceImpl. */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired private UserMapper userMapper;

    /**
     * login by username and password.
     *
     * @param username username
     * @param password pwd
     * @return {@link String}
     */
    @Override
    public String login(String username, String password) throws BaseException {
        User user = this.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new UserNotExistsException();
        }
        if (!user.getPassword().equals(password)) {
            throw new UserPasswordNotMatchException();
        }
        StpUtil.login(user.getId());

        return StpUtil.getTokenValue();
    }

    /**
     * Query the list of assigned user roles.
     *
     * @param user query params
     * @return user list
     */
    @Override
    public List<User> selectAllocatedList(User user) {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * Query the list of unassigned user roles.
     *
     * @param user query params
     * @return user list
     */
    @Override
    public List<User> selectUnallocatedList(User user) {
        return userMapper.selectUnallocatedList(user);
    }
}
