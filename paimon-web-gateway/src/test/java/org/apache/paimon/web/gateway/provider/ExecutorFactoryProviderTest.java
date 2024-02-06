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

package org.apache.paimon.web.gateway.provider;

import org.apache.paimon.web.engine.flink.sql.gateway.executor.FlinkSqlGatewayExecutorFactory;
import org.apache.paimon.web.engine.flink.sql.gateway.model.SessionEntity;
import org.apache.paimon.web.gateway.config.ExecuteConfig;
import org.apache.paimon.web.gateway.enums.TaskType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/** Test for {@link ExecutorFactoryProvider}. */
public class ExecutorFactoryProviderTest {

    @Test
    public void testGetExecutorFactory() {
        ExecuteConfig config =
                ExecuteConfig.builder().sessionEntity(SessionEntity.builder().build()).build();
        TaskType taskType = TaskType.fromValue("FLINK SQL GATEWAY");
        ExecutorFactoryProvider executorFactoryProvider = new ExecutorFactoryProvider(config);
        assertSame(
                FlinkSqlGatewayExecutorFactory.class,
                executorFactoryProvider.getExecutorFactory(taskType).getClass());
    }
}
