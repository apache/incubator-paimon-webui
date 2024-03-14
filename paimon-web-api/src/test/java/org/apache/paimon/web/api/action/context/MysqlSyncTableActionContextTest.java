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

import org.apache.paimon.web.api.exception.ActionException;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The test class of mysql sync table action context in {@link MysqlSyncTableActionContext}. */
public class MysqlSyncTableActionContextTest extends FlinkCdcActionContextTestBase {

    @Test
    public void testBuild() {
        List<String> args =
                MysqlSyncTableActionContext.builder()
                        .warehouse(warehouse)
                        .database(database)
                        .table(table)
                        .build()
                        .getActionArgs();
        List<String> expectedArgs =
                Arrays.asList(
                        "mysql_sync_table",
                        "--warehouse",
                        warehouse,
                        "--database",
                        database,
                        "--table",
                        table);
        assertLinesMatch(expectedArgs, args);
    }

    @Test
    public void testBuildConf() {
        List<String> args =
                MysqlSyncTableActionContext.builder()
                        .warehouse(warehouse)
                        .database(database)
                        .table(table)
                        .partitionKeys("pt")
                        .mysqlConfList(
                                Arrays.asList(
                                        "table-name='source_table'",
                                        "database-name='source_db'",
                                        "password=123456"))
                        .catalogConfList(
                                Arrays.asList("metastore=hive", "uri=thrift://hive-metastore:9083"))
                        .tableConfList(Arrays.asList("bucket=4", "changelog-producer=input"))
                        .build()
                        .getActionArgs();
        List<String> expectedCommands =
                Arrays.asList(
                        "mysql_sync_table",
                        "--warehouse",
                        warehouse,
                        "--database",
                        database,
                        "--table",
                        table,
                        "--partition_keys",
                        "pt",
                        "--catalog_conf",
                        "metastore=hive",
                        "--catalog_conf",
                        "uri=thrift://hive-metastore:9083",
                        "--table_conf",
                        "bucket=4",
                        "--table_conf",
                        "changelog-producer=input",
                        "--mysql_conf",
                        "table-name='source_table'",
                        "--mysql_conf",
                        "database-name='source_db'",
                        "--mysql_conf",
                        "password=123456");
        assertLinesMatch(expectedCommands, args);
    }

    @Test
    public void testBuildError() {
        MysqlSyncTableActionContext context =
                MysqlSyncTableActionContext.builder()
                        .warehouse(warehouse)
                        .database(database)
                        .partitionKeys("pt")
                        .primaryKeys("pt,uid")
                        .mysqlConfList(
                                Arrays.asList(
                                        "table-name='source_table'",
                                        "database-name='source_db'",
                                        "password=123456"))
                        .catalogConfList(
                                Arrays.asList("metastore=hive", "uri=thrift://hive-metastore:9083"))
                        .tableConfList(Arrays.asList("bucket=4", "changelog-producer=input"))
                        .build();
        ActionException actionException =
                assertThrows(ActionException.class, context::getActionArgs);
        assertEquals("table can not be null", actionException.getMessage());
    }
}
