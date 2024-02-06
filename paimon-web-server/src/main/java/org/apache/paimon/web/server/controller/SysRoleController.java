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

import org.apache.paimon.web.server.data.dto.RoleWithUserDTO;
import org.apache.paimon.web.server.data.model.SysRole;
import org.apache.paimon.web.server.data.model.User;
import org.apache.paimon.web.server.data.result.PageR;
import org.apache.paimon.web.server.data.result.R;
import org.apache.paimon.web.server.data.result.enums.Status;
import org.apache.paimon.web.server.service.SysRoleService;
import org.apache.paimon.web.server.service.UserService;
import org.apache.paimon.web.server.util.PageSupport;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

/** role controller. */
@RestController
@RequestMapping("/api/role")
public class SysRoleController {
    @Autowired private SysRoleService roleService;

    @Autowired private UserService userService;

    /** Obtain role views with pagination. */
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public PageR<SysRole> listRoles(SysRole role) {
        IPage<SysRole> page = PageSupport.startPage();
        List<SysRole> list = roleService.listRoles(page, role);
        return PageR.<SysRole>builder().success(true).total(page.getTotal()).data(list).build();
    }

    /** Obtain detailed information based on role number. */
    @SaCheckPermission("system:role:query")
    @GetMapping(value = "/{roleId}")
    public R<SysRole> getRole(@PathVariable Integer roleId) {
        return R.succeed(roleService.getRoleById(roleId));
    }

    /** Add new role. */
    @SaCheckPermission("system:role:add")
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysRole role) {
        if (!roleService.checkRoleNameUnique(role)) {
            return R.failed(Status.ROLE_NAME_IS_EXIST, role.getRoleName());
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.failed(Status.ROLE_KEY_IS_EXIST, role.getRoleKey());
        }

        return roleService.insertRole(role) > 0 ? R.succeed() : R.failed();
    }

    /** Update role info. */
    @SaCheckPermission("system:role:update")
    @PutMapping
    public R<Void> update(@Validated @RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        if (!roleService.checkRoleNameUnique(role)) {
            return R.failed(Status.ROLE_NAME_IS_EXIST, role.getRoleName());
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.failed(Status.ROLE_KEY_IS_EXIST, role.getRoleKey());
        }

        if (roleService.updateRole(role) > 0) {
            // TODO update user permissions cache
            return R.succeed();
        }
        return R.failed();
    }

    /** Update role status. */
    @SaCheckPermission("system:role:update")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        return roleService.updateRoleStatus(role) ? R.succeed() : R.failed();
    }

    /** Delete role. */
    @SaCheckPermission("system:role:delete")
    @DeleteMapping("/{roleIds}")
    public R<Void> delete(@PathVariable Integer[] roleIds) {
        return roleService.deleteRoleByIds(roleIds) > 0 ? R.succeed() : R.failed();
    }

    /** Obtain a list of role selection boxes. */
    @SaCheckPermission("system:role:query")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.succeed(roleService.list());
    }

    /** Query the list of assigned user roles. */
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/allocatedList")
    public PageR<User> allocatedList(@RequestBody RoleWithUserDTO roleWithUserDTO) {
        IPage<RoleWithUserDTO> page = PageSupport.startPage();
        List<User> list = userService.selectAllocatedList(page, roleWithUserDTO);
        return PageR.<User>builder().success(true).total(page.getTotal()).data(list).build();
    }

    /** Query the list of unassigned user roles. */
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/unallocatedList")
    public PageR<User> unallocatedList(@RequestBody RoleWithUserDTO roleWithUserDTO) {
        IPage<RoleWithUserDTO> page = PageSupport.startPage();
        List<User> list = userService.selectUnallocatedList(page, roleWithUserDTO);
        return PageR.<User>builder().success(true).total(page.getTotal()).data(list).build();
    }

    /** Batch Unauthorize User. */
    @SaCheckPermission("system:role:update")
    @PutMapping("/authUser/cancelAll")
    public R<Void> cancelAuthUserAll(Integer roleId, Integer[] userIds) {
        return roleService.deleteAuthUsers(roleId, userIds) > 0 ? R.succeed() : R.failed();
    }

    /** Batch Set User Authorization. */
    @SaCheckPermission("system:role:update")
    @PutMapping("/authUser/selectAll")
    public R<Void> selectAuthUserAll(Integer roleId, Integer[] userIds) {
        return roleService.insertAuthUsers(roleId, userIds) > 0 ? R.succeed() : R.failed();
    }
}
