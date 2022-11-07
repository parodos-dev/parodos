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
 * @author Richard Wang (Github: RichardW98)
 */

import { useContext, useState } from 'react';
import ToastContext from '../contexts/toast';
import { get } from '../utils/api';
import { RunDetailContext } from '../contexts/projects/runDetail';
import { getUrl } from '../utils/getUrl';

const useGetErrorLog = () => {
  const toastContext = useContext(ToastContext);
  const runDetailContext = useContext(RunDetailContext);
  const [isLoadingState, setIsLoadingState] = useState(true);
  const url = getUrl();

  const getLogError = async (projectId, branch) => {
    try {
      if (runDetailContext.pipeline === {}) {
        setIsLoadingState(true);
      }
      const logErrorResponse = await get(
        `/api/v1/pipeline/${projectId.projectId}/branches/${branch}/errorlog`,
        url,
      );
      runDetailContext.setLogError(logErrorResponse.data);
      return logErrorResponse.data;
    } catch (error) {
      toastContext.handleOpenToast(
        `Oops! Something went wrong. Please try again`,
      );
    } finally {
      setIsLoadingState(false);
    }
  };

  return {
    getLog: getLogError,
    isLoading: isLoadingState,
  };
};

export default useGetErrorLog;
