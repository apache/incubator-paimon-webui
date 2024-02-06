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

package org.apache.paimon.web.gateway.enums;

/** The {@code TaskType} enum defines the types of tasks that can be executed. */
public enum TaskType {
    SPARK("SPARK"),
    FLINK_SQL_GATEWAY("FLINK SQL GATEWAY");

    private final String value;

    public static TaskType fromValue(String value) {
        for (TaskType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown task type value: " + value);
    }

    TaskType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
