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

package org.apache.paimon.web.server.service.impl;

import org.apache.paimon.data.InternalRow;
import org.apache.paimon.reader.RecordReader;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.web.api.catalog.PaimonService;
import org.apache.paimon.web.server.constant.MetadataConstant;
import org.apache.paimon.web.server.data.dto.MetadataDTO;
import org.apache.paimon.web.server.data.model.CatalogInfo;
import org.apache.paimon.web.server.data.model.MetadataFieldsModel;
import org.apache.paimon.web.server.data.model.MetadataOptionModel;
import org.apache.paimon.web.server.data.vo.DataFileVO;
import org.apache.paimon.web.server.data.vo.ManifestsVO;
import org.apache.paimon.web.server.data.vo.SchemaVO;
import org.apache.paimon.web.server.data.vo.SnapshotVO;
import org.apache.paimon.web.server.service.CatalogService;
import org.apache.paimon.web.server.service.MetadataService;
import org.apache.paimon.web.server.util.PaimonServiceUtils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** The implementation of {@link MetadataService}. */
@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    private final CatalogService catalogService;

    public MetadataServiceImpl(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    private RecordReader<InternalRow> reader;

    @Override
    public List<SchemaVO> getSchema(MetadataDTO dto) {

        initEnvironment(dto, MetadataConstant.SCHEMAS);

        List<SchemaVO> result = new LinkedList<>();
        try {
            reader.forEachRemaining(
                    internalRow -> {
                        SchemaVO schemaVo =
                                SchemaVO.builder()
                                        .setSchemaId(internalRow.getLong(0))
                                        .setFields(
                                                new Gson()
                                                        .fromJson(
                                                                internalRow.getString(1).toString(),
                                                                new TypeToken<
                                                                        LinkedList<
                                                                                MetadataFieldsModel>>() {}))
                                        .setPartitionKeys(internalRow.getString(2).toString())
                                        .setPrimaryKeys(internalRow.getString(3).toString())
                                        .setOption(
                                                formatOptions(internalRow.getString(4).toString()))
                                        .setComment(internalRow.getString(5).toString())
                                        .setUpdateTime(
                                                internalRow.getTimestamp(6, 3).toLocalDateTime())
                                        .build();
                        result.add(schemaVo);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public List<SnapshotVO> getSnapshot(MetadataDTO dto) {

        initEnvironment(dto, MetadataConstant.SNAPSHOTS);

        List<SnapshotVO> result = new LinkedList<>();

        try {
            reader.forEachRemaining(
                    internalRow -> {
                        SnapshotVO build =
                                SnapshotVO.builder()
                                        .snapshotId(internalRow.getLong(0))
                                        .schemaId(internalRow.getLong(1))
                                        .commitUser(internalRow.getString(2).toString())
                                        .commitIdentifier(internalRow.getLong(3))
                                        .commitKind(internalRow.getString(4).toString())
                                        .commitTime(
                                                internalRow.getTimestamp(5, 3).toLocalDateTime())
                                        .baseManifestList(internalRow.getString(6).toString())
                                        .deltaManifestList(internalRow.getString(7).toString())
                                        .changelogManifestList(
                                                internalRow.getString(8) != null
                                                        ? internalRow.getString(8).toString()
                                                        : "")
                                        .totalRecordCount(internalRow.getLong(9))
                                        .deltaRecordCount(internalRow.getLong(10))
                                        .changelogRecordCount(internalRow.getLong(11))
                                        .addedFileCount(internalRow.getInt(12))
                                        .deletedFileCount(internalRow.getInt(13))
                                        .watermark(internalRow.getLong(14))
                                        .build();
                        result.add(build);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public List<ManifestsVO> getManifest(MetadataDTO dto) {
        initEnvironment(dto, MetadataConstant.MANIFESTS);

        List<ManifestsVO> result = new LinkedList<>();

        try {
            reader.forEachRemaining(
                    internalRow -> {
                        ManifestsVO manifestsVo =
                                ManifestsVO.builder()
                                        .fileName(internalRow.getString(0).toString())
                                        .fileSize(internalRow.getLong(1))
                                        .numAddedFiles(internalRow.getLong(2))
                                        .numDeletedFiles(internalRow.getLong(3))
                                        .schemaId(internalRow.getLong(4))
                                        .build();
                        result.add(manifestsVo);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public List<DataFileVO> getDataFile(MetadataDTO dto) {

        initEnvironment(dto, MetadataConstant.FILES);

        List<DataFileVO> result = new LinkedList<>();

        try {
            reader.forEachRemaining(
                    internalRow -> {
                        DataFileVO dataFileVo =
                                DataFileVO.builder()
                                        .partition(internalRow.getString(0).toString())
                                        .bucket(internalRow.getInt(1))
                                        .filePath(internalRow.getString(2).toString())
                                        .fileFormat(internalRow.getString(3).toString())
                                        .schemaId(internalRow.getLong(4))
                                        .level(internalRow.getInt(5))
                                        .recordCount(internalRow.getLong(6))
                                        .fileSizeInBytes(internalRow.getLong(7))
                                        .minKey(
                                                internalRow.getString(8) != null
                                                        ? internalRow.getString(8).toString()
                                                        : "")
                                        .maxKey(
                                                internalRow.getString(9) != null
                                                        ? internalRow.getString(9).toString()
                                                        : "")
                                        .nullValueCounts(internalRow.getString(10).toString())
                                        .minValueStats(internalRow.getString(11).toString())
                                        .maxValueStats(internalRow.getString(12).toString())
                                        .minSequenceNumber(internalRow.getLong(13))
                                        .maxSequenceNumber(internalRow.getLong(14))
                                        .creationTime(
                                                internalRow.getTimestamp(15, 3).toLocalDateTime())
                                        .build();
                        result.add(dataFileVo);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void initEnvironment(MetadataDTO dto, String metadataConstantType) {
        dto.setTableName(
                String.format(
                        MetadataConstant.METADATA_TABLE_FORMAT,
                        dto.getTableName(),
                        metadataConstantType));
        CatalogInfo catalogInfo =
                catalogService.getOne(
                        Wrappers.lambdaQuery(CatalogInfo.class)
                                .eq(CatalogInfo::getId, dto.getCatalogId())
                                .select(i -> true));
        PaimonService paimonService = PaimonServiceUtils.getPaimonService(catalogInfo);
        Table table = paimonService.getTable(dto.getDatabaseName(), dto.getTableName());
        this.reader = getReader(table);
    }

    private List<MetadataOptionModel> formatOptions(String jsonOption) {
        Gson gson = new Gson();
        Map<String, Object> map =
                gson.fromJson(jsonOption, new TypeToken<Map<String, Object>>() {});
        List<MetadataOptionModel> result = new LinkedList<>();
        for (Object key : map.keySet()) {
            result.add(new MetadataOptionModel(key.toString(), map.get(key)));
        }
        return result;
    }

    private static RecordReader<InternalRow> getReader(Table table) {
        ReadBuilder readBuilder = table.newReadBuilder();
        try (RecordReader<InternalRow> reader =
                readBuilder.newRead().createReader(readBuilder.newScan().plan())) {
            return reader;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
