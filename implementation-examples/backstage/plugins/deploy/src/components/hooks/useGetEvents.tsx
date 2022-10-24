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
import { getEventUrl } from '../utils/getUrl';

const useGetEvents = () => {
  const toastContext = useContext(ToastContext);
  const runDetailContext = useContext(RunDetailContext);
  const [isLoadingState, setIsLoadingState] = useState(false);
  const eventUrl = getEventUrl();

  const getEvents = async (
    projectId,
    branch = runDetailContext.selectedBranch,
    org,
    repo,
  ) => {
    try {
      setIsLoadingState(true);
      const events = await get(
        `/api/v1/projects/${projectId}/branches/${branch}/history?org=${org}&repo=${repo}`,
        eventUrl,
      );
      runDetailContext.setEvents(events.data?.eventList);
    } catch (error) {
      toastContext.handleOpenToast(
        `Oops! Something went wrong. Please try again`,
      );
    } finally {
      setIsLoadingState(false);
    }
  };

  return {
    getEvents,
    isLoading: isLoadingState,
  };
};

export default useGetEvents;
