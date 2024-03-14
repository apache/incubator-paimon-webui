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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The test class of shell execution in{@link ShellService}. */
public class ShellServiceTest {

    @Test
    public void testExecuteShell() throws IOException {
        List<String> args = Arrays.asList("/bin/sh", "-c", "echo 1");
        ShellService shellService = new ShellService("/", args);
        Process execute = shellService.execute();
        InputStream is = execute.getInputStream();
        List<String> lines = IOUtils.readLines(is);
        assertThat(lines.size() == 1 && "1".equals(lines.get(0))).isTrue();
    }

    @Test
    public void testExecuteBash() throws IOException {
        List<String> args = Arrays.asList("/bin/bash", "-c", "echo 1");
        ShellService shellService = new ShellService("/", args);
        Process execute = shellService.execute();
        InputStream is = execute.getInputStream();
        List<String> lines = IOUtils.readLines(is);
        assertThat(lines.size() == 1 && "1".equals(lines.get(0))).isTrue();
    }
}
