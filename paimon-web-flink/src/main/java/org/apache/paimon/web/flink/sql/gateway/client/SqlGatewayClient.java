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

package org.apache.paimon.web.flink.sql.gateway.client;

import org.apache.flink.table.gateway.rest.serde.ResultInfo;
import org.apache.paimon.web.flink.sql.gateway.model.SessionEntity;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.runtime.rest.messages.EmptyMessageParameters;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.table.gateway.api.operation.OperationHandle;
import org.apache.flink.table.gateway.api.results.ResultSet;
import org.apache.flink.table.gateway.api.session.SessionHandle;
import org.apache.flink.table.gateway.rest.header.operation.CancelOperationHeaders;
import org.apache.flink.table.gateway.rest.header.operation.CloseOperationHeaders;
import org.apache.flink.table.gateway.rest.header.operation.GetOperationStatusHeaders;
import org.apache.flink.table.gateway.rest.header.session.CloseSessionHeaders;
import org.apache.flink.table.gateway.rest.header.session.ConfigureSessionHeaders;
import org.apache.flink.table.gateway.rest.header.session.GetSessionConfigHeaders;
import org.apache.flink.table.gateway.rest.header.session.OpenSessionHeaders;
import org.apache.flink.table.gateway.rest.header.session.TriggerSessionHeartbeatHeaders;
import org.apache.flink.table.gateway.rest.header.statement.ExecuteStatementHeaders;
import org.apache.flink.table.gateway.rest.header.statement.FetchResultsHeaders;
import org.apache.flink.table.gateway.rest.message.operation.OperationMessageParameters;
import org.apache.flink.table.gateway.rest.message.session.ConfigureSessionRequestBody;
import org.apache.flink.table.gateway.rest.message.session.GetSessionConfigResponseBody;
import org.apache.flink.table.gateway.rest.message.session.OpenSessionRequestBody;
import org.apache.flink.table.gateway.rest.message.session.SessionMessageParameters;
import org.apache.flink.table.gateway.rest.message.statement.ExecuteStatementRequestBody;
import org.apache.flink.table.gateway.rest.message.statement.FetchResultsMessageParameters;
import org.apache.flink.table.gateway.rest.message.statement.FetchResultsResponseBody;
import org.apache.flink.table.gateway.rest.util.RowFormat;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The client of flink sql gateway provides some operations of flink sql gateway. such as creating
 * session, execute statement, fetch result, etc.
 */
public class SqlGatewayClient {

    private static final String DEFAULT_SESSION_NAME_PREFIX = "FLINK_SQL_GATEWAY_SESSION";
    private static final int REQUEST_WAITE_TIME = 1000;
    private static final Integer ACTIVE_STATUS = 1;
    private static final Integer INACTIVE_STATUS = 0;

    private final SqlGateWayRestClient restClient;
    private final String address;
    private final Integer port;

    public SqlGatewayClient(String address, int port) throws Exception {
        this.address = address;
        this.port = port;
        this.restClient = new SqlGateWayRestClient(address, port);
    }

    public SessionEntity openSession(String sessionName) throws Exception {
        String name =
                StringUtils.isBlank(sessionName)
                        ? DEFAULT_SESSION_NAME_PREFIX + "_" + UUID.randomUUID()
                        : sessionName;

        String sessionId =
                restClient
                        .sendRequest(
                                OpenSessionHeaders.getInstance(),
                                EmptyMessageParameters.getInstance(),
                                new OpenSessionRequestBody(name, new HashMap<>()))
                        .get()
                        .getSessionHandle();

        return SessionEntity.builder()
                .sessionId(sessionId)
                .sessionName(sessionName)
                .address(address)
                .port(port)
                .properties(getSessionConfig(sessionId))
                .status(ACTIVE_STATUS)
                .build();
    }

    public Map<String, String> getSessionConfig(String sessionId) throws Exception {
        GetSessionConfigResponseBody getSessionConfigResponseBody =
                restClient
                        .sendRequest(
                                GetSessionConfigHeaders.getInstance(),
                                new SessionMessageParameters(
                                        buildSessionHandleBySessionId(sessionId)),
                                EmptyRequestBody.getInstance())
                        .get();
        return getSessionConfigResponseBody.getProperties();
    }

    public void configureSession(String sessionId, String statement, Long timeout)
            throws Exception {
        ConfigureSessionRequestBody configureSessionRequestBody =
                new ConfigureSessionRequestBody(statement, timeout);
        restClient
                .sendRequest(
                        ConfigureSessionHeaders.getInstance(),
                        new SessionMessageParameters(buildSessionHandleBySessionId(sessionId)),
                        configureSessionRequestBody)
                .get();
    }

    public boolean closeSession(String sessionId) {
        try {
            restClient
                    .sendRequest(
                            CloseSessionHeaders.getInstance(),
                            new SessionMessageParameters(buildSessionHandleBySessionId(sessionId)),
                            EmptyRequestBody.getInstance())
                    .get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer triggerSessionHeartbeat(String sessionId) {
        try {
            restClient
                    .sendRequest(
                            TriggerSessionHeartbeatHeaders.getInstance(),
                            new SessionMessageParameters(buildSessionHandleBySessionId(sessionId)),
                            EmptyRequestBody.getInstance())
                    .get();
            return ACTIVE_STATUS;
        } catch (Exception e) {
            return INACTIVE_STATUS;
        }
    }

    public String executeStatement(String sessionId, String statement, @Nullable Long timeout)
            throws Exception {
        return restClient
                .sendRequest(
                        ExecuteStatementHeaders.getInstance(),
                        new SessionMessageParameters(buildSessionHandleBySessionId(sessionId)),
                        new ExecuteStatementRequestBody(statement, timeout, new HashMap<>()))
                .get()
                .getOperationHandle();
    }

    public FetchResultsResponseBody fetchResults(String sessionId, String operationId, long token)
            throws Exception {
        FetchResultsResponseBody fetchResultsResponseBody =
                restClient
                        .sendRequest(
                                FetchResultsHeaders.getDefaultInstance(),
                                new FetchResultsMessageParameters(
                                        buildSessionHandleBySessionId(sessionId),
                                        buildOperationHandleByOperationId(operationId),
                                        token,
                                        RowFormat.JSON),
                                EmptyRequestBody.getInstance())
                        .get();
        ResultSet.ResultType resultType = fetchResultsResponseBody.getResultType();
        while (resultType == ResultSet.ResultType.NOT_READY) {
            fetchResultsResponseBody =
            restClient
                    .sendRequest(
                            FetchResultsHeaders.getDefaultInstance(),
                            new FetchResultsMessageParameters(
                                    buildSessionHandleBySessionId(sessionId),
                                    buildOperationHandleByOperationId(operationId),
                                    token,
                                    RowFormat.JSON),
                            EmptyRequestBody.getInstance())
                    .get();
            resultType = fetchResultsResponseBody.getResultType();
            TimeUnit.MILLISECONDS.sleep(REQUEST_WAITE_TIME);
        }
        return fetchResultsResponseBody;
    }

    private String getOperationStatus(String sessionId, String operationId) throws Exception {
        return restClient
                .sendRequest(
                        GetOperationStatusHeaders.getInstance(),
                        new OperationMessageParameters(
                                buildSessionHandleBySessionId(sessionId),
                                buildOperationHandleByOperationId(operationId)),
                        EmptyRequestBody.getInstance())
                .get()
                .getStatus();
    }

    public boolean cancelOperation(String sessionId, String operationId) {
        try {
            restClient
                    .sendRequest(
                            CancelOperationHeaders.getInstance(),
                            new OperationMessageParameters(
                                    buildSessionHandleBySessionId(sessionId),
                                    buildOperationHandleByOperationId(operationId)),
                            EmptyRequestBody.getInstance())
                    .get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean closeOperation(String sessionId, String operationId) {
        try {
            restClient
                    .sendRequest(
                            CloseOperationHeaders.getInstance(),
                            new OperationMessageParameters(
                                    buildSessionHandleBySessionId(sessionId),
                                    buildOperationHandleByOperationId(operationId)),
                            EmptyRequestBody.getInstance())
                    .get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SessionHandle buildSessionHandleBySessionId(String sessionId) {
        return new SessionHandle(UUID.fromString(sessionId));
    }

    private OperationHandle buildOperationHandleByOperationId(String operationId) {
        return new OperationHandle(UUID.fromString(operationId));
    }
}
