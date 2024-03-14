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

package org.apache.paimon.web.api.action.service;

import org.apache.paimon.web.api.action.context.ActionContext;
import org.apache.paimon.web.api.action.context.FlinkActionContext;
import org.apache.paimon.web.api.enums.ActionExecuteResult;
import org.apache.paimon.web.api.enums.FlinkJobType;
import org.apache.paimon.web.api.exception.ActionException;
import org.apache.paimon.web.api.shell.ShellService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** An abstract Action service that executes actions through the shell. */
@Slf4j
public class FlinkCdcActionService implements ActionService {

    private List<String> getCommand(FlinkActionContext actionContext) {
        List<String> commandList = new ArrayList<>();
        commandList.add("bin/flink");
        commandList.add("run");
        if (actionContext.getFlinkJobType() != FlinkJobType.SESSION) {
            throw new ActionException("Only support session job now.");
        }
        String sessionUrl = actionContext.getSessionUrl();
        if (StringUtils.isNotBlank(sessionUrl)) {
            commandList.add("-m");
            commandList.add(sessionUrl);
        }
        commandList.add(actionContext.getActionJarPath());
        commandList.addAll(actionContext.getActionArgs());
        return commandList;
    }

    public void execute(ActionContext actionContext) throws Exception {
        String flinkHome = getFlinkHome();
        FlinkActionContext flinkActionContext;
        if (!(actionContext instanceof FlinkActionContext)) {
            throw new ActionException("Only support FlinkActionContext. ");
        }
        flinkActionContext = (FlinkActionContext) actionContext;
        try {
            List<String> command = getCommand(flinkActionContext);
            Process process = new ShellService(flinkHome, command).execute();
            flinkActionContext.setExecuteResult(ActionExecuteResult.SUCCESS);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            flinkActionContext.setErrorMessage(exception.getMessage());
            flinkActionContext.setExecuteResult(ActionExecuteResult.FAILED);
        }
    }

    private String getFlinkHome() {
        String flinkHome = System.getenv("FLINK_HOME");
        if (StringUtils.isBlank(flinkHome)) {
            flinkHome = System.getProperty("FLINK_HOME");
        }
        if (StringUtils.isBlank(flinkHome)) {
            throw new ActionException("FLINK_HOME is null");
        }
        return flinkHome;
    }
}
