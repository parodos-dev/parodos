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

import React, { useContext, useEffect } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Grid,
  Typography,
} from '@material-ui/core';

import ArrowBackIcon from '@material-ui/icons/ArrowBack';
import { RunDetailContext } from '../../contexts/projects/runDetail';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import RunDetailContent from './RunDetailContent';
import useGetBranches from '../../hooks/useGetBranches';
import useGetPipeline from '../../hooks/useGetPipeline';
import useGetEvents from '../../hooks/useGetEvents';
import ConfigureEnvDialog from './RunDetailContent/TabPanels/ProcessesPanel/Pipeline/PipelineStepper/PipelineStep/ConfigureEnvDialog';
import ErrorLogDialog from './RunDetailContent/TabPanels/ProcessesPanel/Pipeline/PipelineStepper/PipelineStep/ErrorLogDialog';
import PipelineLogViewer from './PipelineLogViewer';

export default function RunDetail() {
  let navigate = useNavigate();
  let { projectId } = useParams();
  let { state } = useLocation();
  const runDetailContext = useContext(RunDetailContext);
  const useGetBranchHook = useGetBranches();
  const useGetPipelineHook = useGetPipeline();
  const useGetEventHook = useGetEvents();

  useEffect(() => {
    runDetailContext.setProjectId(projectId);
    runDetailContext.setSelectedProject(state);
    useGetBranchHook.getBranches(state).then(branches => {
      console.log('branches:', branches);
      runDetailContext.setSelectedBranch(branches[0]);
      useGetPipelineHook.getPipeline(projectId, branches[0]);
      useGetEventHook.getEvents(
        projectId,
        branches[0],
        state.organizationName,
        state.repositoryName,
      );
    });
  }, []);

  useEffect(() => {
    useGetPipelineHook.getPipeline(projectId);
    const interval = setInterval(() => {
      console.log(
        'updating pipeline for',
        state.organizationName,
        '/',
        state.repositoryName,
        ':',
        runDetailContext.selectedBranch,
        ' ...',
      );
      useGetPipelineHook.getPipeline(projectId);
      console.log('pipeline updated!');
      useGetEventHook.getEvents(
        projectId,
        runDetailContext.selectedBranch,
        state.organizationName,
        state.repositoryName,
      );
    }, 5000);
    return () => {
      clearInterval(interval);
      runDetailContext.setFilter('all');
    };
  }, [runDetailContext.selectedBranch]);

  const handleClickBack = () => {
    navigate('/');
  };

  const handleUpdated = () => {};

  const handleToggleDialog = (error = false) => {
    if (!error) {
      runDetailContext.setOpen(!runDetailContext.open);
    } else {
      runDetailContext.setOpenErrorLog(!runDetailContext.openErrorLog);
    }
  };

  return (
    <Box>
      <Grid style={{ width: '100%' }}>
        <Grid
          container
          spacing={2}
          style={{ marginTop: 15, marginLeft: 15, width: '95%' }}
        >
          <Grid container style={{ marginTop: 3, marginLeft: 1 }}>
            <Button
              variant="text"
              onClick={handleClickBack}
              color={'secondary'}
              startIcon={
                <ArrowBackIcon fontSize={'medium'} color={'primary'} />
              }
            >
              <Typography variant={'body1'}>back to projects</Typography>
            </Button>
          </Grid>
          <Grid container mt={3} ml={11} mr={11}>
            {useGetBranchHook.isLoading || useGetPipelineHook.isLoading ? (
              <Grid
                item
                width="100%"
                pt="20%"
                display="flex"
                style={{ alignItems: 'center', justifyContent: 'center' }}
              >
                <CircularProgress color="primary" size={100} />
              </Grid>
            ) : (
              <RunDetailContent />
            )}
          </Grid>

          <ConfigureEnvDialog
            handleClose={() => handleToggleDialog()}
            {...{ stageName: runDetailContext.stage, handleUpdated }}
          />

          <ErrorLogDialog
            handleClose={() => handleToggleDialog(true)}
            {...{ stageName: runDetailContext.stage, handleUpdated }}
          />
        </Grid>

        <Grid container style={{ position: 'fixed', width: '100%', bottom: 0 }}>
          {runDetailContext.openLog ? (
            <Box width={'95%'}>
              <PipelineLogViewer projectId={projectId} />
            </Box>
          ) : (
            <div></div>
          )}
        </Grid>
      </Grid>
    </Box>
  );
}
