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

package org.apache.paimon.web.flink.context;

import org.apache.paimon.web.flink.common.ExecutionMode;
import org.apache.paimon.web.flink.context.params.RemoteParams;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/** The RemoteExecutorContext class provides the context for creating a RemoteExecutor. */
public class RemoteExecutorContext extends ExecutorContext {

    private final RemoteParams parameters;

    public RemoteExecutorContext(
            RemoteParams parameters, Configuration configuration, ExecutionMode mode) {
        super(configuration, mode);
        this.parameters = parameters;
        init();
    }

    @Override
    protected StreamExecutionEnvironment createEnvironment() {
        StreamExecutionEnvironment env;
        String jarFilePath =
                StringUtils.isNotBlank(parameters.getJarFilePath())
                        ? parameters.getJarFilePath()
                        : null;

        if (configuration != null && jarFilePath != null) {
            env =
                    StreamExecutionEnvironment.createRemoteEnvironment(
                            parameters.getHost(), parameters.getPort(), configuration, jarFilePath);
        } else if (configuration != null) {
            env =
                    StreamExecutionEnvironment.createRemoteEnvironment(
                            parameters.getHost(), parameters.getPort(), configuration);
        } else if (jarFilePath != null) {
            env =
                    StreamExecutionEnvironment.createRemoteEnvironment(
                            parameters.getHost(), parameters.getPort(), jarFilePath);
        } else {
            env =
                    StreamExecutionEnvironment.createRemoteEnvironment(
                            parameters.getHost(), parameters.getPort());
        }
        return env;
    }
}
