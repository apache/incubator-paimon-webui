/* Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License. */

import { Form } from '@douyinfe/semi-ui';
import {useTranslation} from "react-i18next";

// @ts-ignore
const CatalogForm = ({ getFormApi }) => {
    const { t } = useTranslation();
    return(
       <>
           <Form
               wrapperCol={{ span: 20 }}
               labelCol={{ span: 4 }}
               labelPosition='left'
               labelAlign='left'
               getFormApi={getFormApi}
           >
               {
                   ({ formState }) => (
                       <>
                           <Form.Input
                               field="catalogName"
                               label='Catalog Name'
                               trigger='blur'
                               rules={[
                                   { required: true, message: t('metadata.message') },
                               ]}
                               style={{ width: "100%" }}
                               showClear/>

                           <Form.Select
                               field="catalogType"
                               label='Catalog Type'
                               placeholder={"please select catalog"}
                               rules={[
                                   { required: true, message: t('metadata.message') },
                               ]}
                               style={{ width: "100%" }}
                               showClear>
                               <Form.Select.Option value="filesystem">FileSystem</Form.Select.Option>
                               <Form.Select.Option value="hive">Hive</Form.Select.Option>
                           </Form.Select>

                           {formState.values.catalogType === 'filesystem' ? (
                               <Form.Input
                                   field="warehouse"
                                   label='Warehouse'
                                   trigger='blur'
                                   rules={[
                                       { required: true, message: t('metadata.message') },
                                   ]}
                                   style={{ width: "100%" }}
                                   showClear/>
                           )
                               :
                               <>
                                   <Form.Input
                                       field="warehouse"
                                       label='Warehouse'
                                       trigger='blur'
                                       rules={[
                                           { required: true, message: t('metadata.message') },
                                       ]}
                                       style={{ width: "100%" }}
                                       showClear/>

                                   <Form.Input
                                       field="uri"
                                       label='Hive Uri'
                                       trigger='blur'
                                       rules={[
                                           { required: true, message: t('metadata.message') },
                                       ]}
                                       style={{ width: "100%" }}
                                       showClear/>

                                   <Form.Input
                                       field="hiveConfDir"
                                       label='Hive Conf Dir'
                                       trigger='blur'
                                       rules={[
                                           { required: true, message: t('metadata.message') },
                                       ]}
                                       style={{ width: "100%" }}
                                       showClear/>
                               </>
                           }
                       </>
                   )
               }
           </Form>
       </>
    );
}

export default CatalogForm;