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

import lombok.experimental.SuperBuilder;

import java.util.List;

/** The FlinkCdcDatabasesActionContext for the entire database synchronization. */
@SuperBuilder
public abstract class FlinkCdcDatabasesSyncActionContext extends FlinkActionContext
        implements ActionContext {

    @ActionConf(value = "warehouse", nullable = false)
    protected String warehouse;

    @ActionConf(value = "database", nullable = false)
    protected String database;

    @ActionConf(value = "catalog_conf", confList = true)
    protected List<String> catalogConfList;

    @ActionConf(value = "table_conf", confList = true)
    protected List<String> tableConfList;
}
