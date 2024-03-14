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

package org.apache.paimon.web.api.action.context;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/** ActionContext Util. */
public class ActionContextUtil {

    private ActionContextUtil() {}

    public static void addConf(List<String> args, String confName, String conf) {
        if (StringUtils.isNotBlank(conf)) {
            args.add("--" + confName);
            args.add(conf);
        }
    }

    public static void addConfList(List<String> args, String confName, List<String> confList) {
        if (confList != null && !confList.isEmpty()) {
            for (String conf : confList) {
                addConf(args, confName, conf);
            }
        }
    }
}
