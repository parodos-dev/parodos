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

import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

export const RunDetailContext = createContext();

export const RunDetailProvider = ({ children }) => {
  const [selectedProject, setSelectedProject] = useState(null);
  const [projectId, setProjectId] = useState(null);
  const [branches, setBranches] = useState([]);
  const [selectedBranch, setSelectedBranch] = useState(null);
  const [pipeline, setPipeline] = useState({});
  const [filter, setFilter] = useState('all');
  const [events, setEvents] = useState([]);
  const [fetchPipelineRequest, setFetchPipelineRequest] = useState(false);
  const [fetchPipelineSuccess, setFetchPipelineSuccess] = useState(false);
  const [fetchPipelineError, setFetchPipelineError] = useState(false);
  const [addedAdgroupEmployees, setAddedAdgroupEmployees] = useState([]);
  const [env, setEnv] = useState({});
  const [open, setOpen] = useState(false);
  const [openErrorLog, setOpenErrorLog] = useState(false);
  const [stage, setStage] = useState('');
  const [openLog, setOpenLog] = useState(false);
  const [piplineLog, setPipelineLog] = useState('');
  return (
    <RunDetailContext.Provider
      value={{
        selectedProject,
        setSelectedProject,
        projectId,
        setProjectId,
        branches,
        setBranches,
        selectedBranch,
        setSelectedBranch,
        pipeline,
        setPipeline,
        filter,
        setFilter,
        events,
        setEvents,
        fetchPipelineSuccess,
        setFetchPipelineSuccess,
        fetchPipelineRequest,
        setFetchPipelineRequest,
        fetchPipelineError,
        setFetchPipelineError,
        addedAdgroupEmployees,
        setAddedAdgroupEmployees,
        env,
        setEnv,
        open,
        setOpen,
        openErrorLog,
        setOpenErrorLog,
        stage,
        setStage,
        openLog,
        setOpenLog,
        piplineLog,
        setPipelineLog,
      }}
    >
      {children}
    </RunDetailContext.Provider>
  );
};

RunDetailProvider.propTypes = {
  children: PropTypes.element.isRequired,
};
