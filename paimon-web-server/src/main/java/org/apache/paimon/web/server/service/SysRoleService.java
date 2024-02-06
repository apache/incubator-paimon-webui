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

package org.apache.paimon.web.server.service;

import org.apache.paimon.web.server.data.model.SysRole;
import org.apache.paimon.web.server.data.model.UserRole;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/** Role service. */
public interface SysRoleService extends IService<SysRole> {
    /**
     * Paging and querying role data based on conditions.
     *
     * @param page page params
     * @param role query params
     * @return role list
     */
    List<SysRole> listRoles(IPage<SysRole> page, SysRole role);

    /**
     * Query role list based on user ID.
     *
     * @param userId user ID
     * @return role list
     */
    List<SysRole> selectRolesByUserId(Integer userId);

    /**
     * Query role permissions based on user ID.
     *
     * @param userId user ID
     * @return permissions
     */
    Set<String> selectRolePermissionByUserId(Integer userId);

    /**
     * Query user role list by user ID.
     *
     * @param userId user ID
     * @return role list
     */
    List<Integer> selectRoleListByUserId(Integer userId);

    /**
     * Query role info by role ID.
     *
     * @param roleId role ID
     * @return role info
     */
    SysRole getRoleById(Integer roleId);

    /**
     * Verify if the role name is unique.
     *
     * @param role role info
     * @return result
     */
    boolean checkRoleNameUnique(SysRole role);

    /**
     * Verify whether role permissions are unique.
     *
     * @param role role info
     * @return result
     */
    boolean checkRoleKeyUnique(SysRole role);

    /**
     * Verify whether the role allows operations.
     *
     * @param role role info
     */
    boolean checkRoleAllowed(SysRole role);

    /**
     * Save role information.
     *
     * @param role role info
     * @return result
     */
    int insertRole(SysRole role);

    /**
     * Update role information.
     *
     * @param role role info
     * @return result
     */
    int updateRole(SysRole role);

    /**
     * Delete role through role ID.
     *
     * @param roleId role ID
     * @return result
     */
    int deleteRoleById(Integer roleId);

    /**
     * Batch delete role information.
     *
     * @param roleIds role IDs
     * @return result
     */
    int deleteRoleByIds(Integer[] roleIds);

    /**
     * Unauthorize user role.
     *
     * @param userRole user-role
     * @return result
     */
    int deleteAuthUser(UserRole userRole);

    /**
     * Batch unauthorization of user roles.
     *
     * @param roleId role ID
     * @param userIds user IDs that needs to be unlicensed
     * @return 结果
     */
    int deleteAuthUsers(Integer roleId, Integer[] userIds);

    /**
     * Batch selection of authorized user roles.
     *
     * @param roleId role ID
     * @param userIds user IDs that needs to be deleted
     * @return result
     */
    int insertAuthUsers(Integer roleId, Integer[] userIds);

    /**
     * Query the number of role usage through role ID.
     *
     * @param roleId role ID
     * @return result
     */
    int countUserRoleByRoleId(Integer roleId);

    /**
     * Update Role Status.
     *
     * @param role role info
     * @return result
     */
    boolean updateRoleStatus(SysRole role);
}
