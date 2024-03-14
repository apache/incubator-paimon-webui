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

package org.apache.paimon.web.api.shell;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Shell service. */
@Slf4j
public class ShellService {

    private final String workingDirectory;
    private final List<String> executeCommand;

    public ShellService(String workingDirectory, List<String> executeCommand) {
        this.workingDirectory = workingDirectory;
        this.executeCommand = executeCommand;
    }

    public Process execute() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(workingDirectory));
        processBuilder.redirectErrorStream(true);
        processBuilder.command(executeCommand);
        log.info("Executing shell command : {}", String.join(" ", executeCommand));
        return processBuilder.start();
    }
}
