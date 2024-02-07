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
import org.apache.paimon.web.server.data.vo.UserVO;
import org.apache.paimon.web.server.util.ObjectMapperUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Test for {@link UserController}. */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest extends ControllerTestBase {

    private static final String userPath = "/api/user";

    private static final int userId = 3;
    private static final String username = "test";

    @Test
    @Order(1)
    public void testAddUser() throws Exception {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setNickname(username);
        user.setUserType(0);
        user.setEnabled(true);
        user.setIsDelete(false);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(userPath)
                                .cookie(cookie)
                                .content(ObjectMapperUtils.toJSON(user))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(2)
    public void testGetUser() throws Exception {
        String responseString =
                mockMvc.perform(
                                MockMvcRequestBuilders.get(userPath + "/" + userId)
                                        .cookie(cookie)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .accept(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        R<UserVO> r = ObjectMapperUtils.fromJSON(responseString, new TypeReference<R<UserVO>>() {});
        assertEquals(200, r.getCode());
        assertNotNull(r.getData());
        assertEquals(r.getData().getUsername(), username);
    }

    @Test
    @Order(3)
    public void testUpdateUser() throws Exception {
        String newUserName = username + "-edit";
        User user = new User();
        user.setId(userId);
        user.setUsername(newUserName);
        user.setNickname(newUserName);
        user.setUserType(0);
        user.setEnabled(true);
        user.setIsDelete(false);

        mockMvc.perform(
                        MockMvcRequestBuilders.put(userPath)
                                .cookie(cookie)
                                .content(ObjectMapperUtils.toJSON(user))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk());

        String responseString =
                mockMvc.perform(
                                MockMvcRequestBuilders.get(userPath + "/" + userId)
                                        .cookie(cookie)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .accept(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        R<UserVO> r = ObjectMapperUtils.fromJSON(responseString, new TypeReference<R<UserVO>>() {});
        assertEquals(200, r.getCode());
        assertNotNull(r.getData());
        assertEquals(r.getData().getUsername(), newUserName);
    }

    @Test
    @Order(4)
    public void testListUsers() throws Exception {
        String responseString =
                mockMvc.perform(
                                MockMvcRequestBuilders.get(userPath + "/list")
                                        .cookie(cookie)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .accept(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PageR<?> r = ObjectMapperUtils.fromJSON(responseString, PageR.class);
        assertNotNull(r);
        assertTrue(
                r.getData() != null
                        && ((r.getTotal() > 0 && r.getData().size() > 0)
                                || (r.getTotal() == 0 && r.getData().size() == 0)));
    }

    @Test
    @Order(5)
    public void testDeleteUser() throws Exception {
        String delResponseString =
                mockMvc.perform(
                                MockMvcRequestBuilders.delete(
                                                userPath + "/" + userId + "," + userId)
                                        .cookie(cookie)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .accept(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        R<?> result = ObjectMapperUtils.fromJSON(delResponseString, R.class);
        assertEquals(200, result.getCode());
    }
}
