/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.web.server.controller;

import org.apache.paimon.web.server.data.model.User;
import org.apache.paimon.web.server.data.result.PageR;
import org.apache.paimon.web.server.data.result.R;
import org.apache.paimon.web.server.data.result.enums.Status;
import org.apache.paimon.web.server.data.vo.UserVO;
import org.apache.paimon.web.server.service.UserService;
import org.apache.paimon.web.server.util.PageSupport;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.apache.paimon.web.server.data.result.enums.Status.USER_NOT_EXIST;

/** User api controller. */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserService userService;

    /**
     * Get user by id.
     *
     * @param id user-id
     * @return {@link R} with {@link UserVO}
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/{id}")
    public R<UserVO> getUser(@PathVariable("id") Integer id) {
        UserVO user = userService.getUserById(id);
        if (user == null) {
            return R.failed(USER_NOT_EXIST);
        }
        return R.succeed(user);
    }

    /**
     * Get user views with pagination.
     *
     * @param user filter conditions
     * @return paginated user view objects
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public PageR<UserVO> listUsers(User user) {
        IPage<User> page = PageSupport.startPage();
        List<UserVO> list = userService.listUsers(page, user);
        return PageR.<UserVO>builder().success(true).total(page.getTotal()).data(list).build();
    }

    /**
     * Add a new user.
     *
     * @param user the user to be added, must not be null
     * @return a {@code R<Void>} response indicating success or failure
     */
    @SaCheckPermission("system:user:add")
    @PostMapping
    public R<Void> add(@Validated @RequestBody User user) {
        if (!userService.checkUserNameUnique(user)) {
            return R.failed(Status.USER_NAME_ALREADY_EXISTS, user.getUsername());
        }

        return userService.insertUser(user) > 0 ? R.succeed() : R.failed();
    }

    /**
     * Update an existing user's details.
     *
     * @param user the user with updated details, must not be null
     * @return a {@code R<Void>} response indicating success or failure
     */
    @SaCheckPermission("system:user:update")
    @PutMapping
    public R<Void> update(@Validated @RequestBody User user) {
        if (!userService.checkUserNameUnique(user)) {
            return R.failed(Status.USER_NAME_ALREADY_EXISTS, user.getUsername());
        }

        return userService.updateUser(user) > 0 ? R.succeed() : R.failed();
    }

    /**
     * Delete one or more users by user ID.
     *
     * @param userIds an array of user IDs to be deleted
     * @return a {@code R<Void>} response indicating success or failure
     */
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{userIds}")
    public R<Void> delete(@PathVariable Integer[] userIds) {
        return userService.deleteUserByIds(userIds) > 0 ? R.succeed() : R.failed();
    }
}
