/*
 *  Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * @author Luke Shannon (Github: lshannon)
 */

import { useContext, useState } from 'react';
import * as R from 'ramda';
import ToastContext from '../context/toast';
import axios from 'axios';
import { getUrl } from '../util/getUrl';

const useGetInfrastructureOptions = () => {
  const toastContext = useContext(ToastContext);
  const [isLoadingState, setIsLoadingState] = useState(false);
  const [migrationOptionsState, setMigrationOptionsState] = useState({});
  const url = getUrl();

  const getMigrationOptions = async ({ orgName, repo }) => {
    try {
      setIsLoadingState(true);
      const assessmentRequestBody = {
        workflowName: 'MyAssessment_ASSESSMENT_WORKFLOW',
        assessmentProperties: {},
      };
      const migrationOptionsResponse = await axios.post(
        `${url}/api/assessments/`,
        assessmentRequestBody,
      );
      setMigrationOptionsState(migrationOptionsResponse.data);
      return migrationOptionsResponse.data;
    } catch (error) {
      toastContext.handleOpenToast(
        `Oops! Something went wrong. Please try again`,
      );
    } finally {
      setIsLoadingState(false);
    }
  };

  return {
    getMigrationOptions,
    isLoading: isLoadingState,
    migrationOptions: migrationOptionsState,
  };
};

export default useGetInfrastructureOptions;
