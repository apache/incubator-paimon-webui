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

package org.apache.paimon.web.server.service.impl;

import org.apache.paimon.web.server.data.model.RoleMenu;
import org.apache.paimon.web.server.data.model.SysRole;
import org.apache.paimon.web.server.data.model.UserRole;
import org.apache.paimon.web.server.data.result.exception.role.RoleInUsedException;
import org.apache.paimon.web.server.mapper.RoleMenuMapper;
import org.apache.paimon.web.server.mapper.SysRoleMapper;
import org.apache.paimon.web.server.mapper.UserRoleMapper;
import org.apache.paimon.web.server.service.SysRoleService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Role Service. */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    @Autowired private SysRoleMapper roleMapper;

    @Autowired private RoleMenuMapper roleMenuMapper;

    @Autowired private UserRoleMapper userRoleMapper;

    /**
     * Query role list.
     *
     * @param role query params
     * @return role list
     */
    @Override
    public List<SysRole> listRoles(IPage<SysRole> page, SysRole role) {
        return roleMapper.selectRoleList(page, role);
    }

    /**
     * Query role list by user ID.
     *
     * @param userId user ID
     * @return role list
     */
    @Override
    public List<SysRole> selectRolesByUserId(Integer userId) {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = this.list();
        for (SysRole role : roles) {
            for (SysRole userRole : userRoles) {
                if (role.getId().intValue() == userRole.getId().intValue()) {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * Query role permission by user ID.
     *
     * @param userId user ID
     * @return permission list
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Integer userId) {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (perm != null) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * Query role list by user ID.
     *
     * @param userId user ID
     * @return role IDs
     */
    @Override
    public List<Integer> selectRoleListByUserId(Integer userId) {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * Query role info by role ID.
     *
     * @param roleId role ID
     * @return role info
     */
    @Override
    public SysRole getRoleById(Integer roleId) {
        return this.getById(roleId);
    }

    /**
     * Verify if the role name is unique.
     *
     * @param role role info
     * @return result
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role) {
        int roleId = role.getId() == null ? -1 : role.getId();
        SysRole info = this.lambdaQuery().eq(SysRole::getRoleName, role.getRoleName()).one();
        return info == null || info.getId() == roleId;
    }

    /**
     * Verify whether role permissions are unique.
     *
     * @param role role info
     * @return result
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role) {
        int roleId = role.getId() == null ? -1 : role.getId();
        SysRole info = this.lambdaQuery().eq(SysRole::getRoleKey, role.getRoleKey()).one();
        return info == null || info.getId() == roleId;
    }

    /**
     * Verify whether the role allows operations.
     *
     * @param role role info
     */
    @Override
    public boolean checkRoleAllowed(SysRole role) {
        return role.getId() != null && role.getId() == 1;
    }

    /**
     * Add role.
     *
     * @param role role info
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRole role) {
        this.save(role);
        return insertRoleMenu(role);
    }

    /**
     * Update role.
     *
     * @param role role info
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRole role) {
        this.updateById(role);
        roleMenuMapper.deleteRoleMenuByRoleId(role.getId());
        return insertRoleMenu(role);
    }

    /**
     * Add role-menu association information.
     *
     * @param role role info
     */
    public int insertRoleMenu(SysRole role) {
        int rows = 1;
        if (role.getMenuIds() != null && role.getMenuIds().length > 0) {
            List<RoleMenu> list = new ArrayList<RoleMenu>();
            for (Integer menuId : role.getMenuIds()) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                list.add(rm);
            }
            if (list.size() > 0) {
                rows = roleMenuMapper.batchRoleMenu(list);
            }
        }
        return rows;
    }

    /**
     * Delete role.
     *
     * @param roleId role ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleById(Integer roleId) {
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        return roleMapper.deleteById(roleId);
    }

    /**
     * Batch delete role.
     *
     * @param roleIds role IDs
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(Integer[] roleIds) {
        for (Integer roleId : roleIds) {
            SysRole sysRole = new SysRole();
            sysRole.setId(roleId);
            checkRoleAllowed(sysRole);
            SysRole role = getRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new RoleInUsedException(role.getRoleName());
            }
        }
        roleMenuMapper.deleteRoleMenu(roleIds);
        return roleMapper.deleteBatchIds(Arrays.asList(roleIds));
    }

    /**
     * Unauthorize user role.
     *
     * @param userRole user-role association
     * @return result
     */
    @Override
    public int deleteAuthUser(UserRole userRole) {
        return userRoleMapper.deleteById(userRole);
    }

    /**
     * Batch unauthorize user role.
     *
     * @param roleId role ID
     * @param userIds user IDs
     * @return result
     */
    @Override
    public int deleteAuthUsers(Integer roleId, Integer[] userIds) {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * Batch add role-menu association information.
     *
     * @param roleId role ID
     * @param userIds user IDs
     * @return result
     */
    @Override
    public int insertAuthUsers(Integer roleId, Integer[] userIds) {
        List<UserRole> list = new ArrayList<UserRole>();
        for (Integer userId : userIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }

    /**
     * Query the number of roles used by role ID.
     *
     * @param roleId role ID
     * @return result
     */
    @Override
    public int countUserRoleByRoleId(Integer roleId) {
        return userRoleMapper
                .selectCount(new QueryWrapper<UserRole>().eq("role_id", roleId))
                .intValue();
    }

    @Override
    public boolean updateRoleStatus(SysRole role) {
        Preconditions.checkArgument(role != null && role.getId() != null);
        return this.lambdaUpdate()
                .set(SysRole::getEnabled, role.getEnabled())
                .eq(SysRole::getId, role.getId())
                .update();
    }
}
