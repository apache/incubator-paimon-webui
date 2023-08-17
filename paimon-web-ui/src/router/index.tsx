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

/*import { lazy } from 'react'*/
import { RouteObject } from 'react-router';
import { useRoutes } from 'react-router-dom';
import LayoutPage from '@src/pages/Layout';
import PlaygroundPage from '@src/pages/Playground';
import MetaDataPage from '@pages/Metadata';
import DevStatus from "@pages/Abnormal/Dev";
import {CdcIngestion} from "@pages/CdcIngestion";

/*const Editor = lazy(() => import('@src/pages/Playground'))
const Studio = lazy(() => import('@src/pages/Metadata'))*/

const routeList: RouteObject[] = [
    {
        path: '/',
        element: <LayoutPage/>,
        children: [
            {
                path: 'playground',
                element: <PlaygroundPage/>
            },
            {
                path: 'metadata',
                element: <MetaDataPage/>
            },
            {
                path: 'cdc-ingestion',
                element: <CdcIngestion/>
            },
            {
                path: 'system',
                element: <DevStatus/>
            }
        ]
    }
]

function RenderRouter() {
    const element = useRoutes(routeList)
    return element
}

export default RenderRouter
