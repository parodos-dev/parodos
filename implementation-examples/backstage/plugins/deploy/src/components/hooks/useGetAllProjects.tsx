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
import { ProjectsContext } from '../contexts/projects/projects';
import { get } from '../utils/api';
import { getUrl } from '../utils/getUrl';

const useGetAllProjects = () => {
  const toastContext = useContext(ToastContext);
  const projectContext = useContext(ProjectsContext);
  const [isLoadingState, setIsLoadingState] = useState(false);
  const url = getUrl();

  const getAllProjects = async () => {
    try {
      setIsLoadingState(true);
      const allProjectsResponse = await get(`/api/v1/projects`, url);
      projectContext.setAllProjects(allProjectsResponse.data);
      // projectContext.setAllProjects(mockProjects);
    } catch (error) {
      toastContext.handleOpenToast(
        `Oops! Something went wrong. Please try again`,
      );
    } finally {
      setIsLoadingState(false);
    }
  };

  return {
    getAllProjects,
    isLoading: isLoadingState,
  };
};

export default useGetAllProjects;
