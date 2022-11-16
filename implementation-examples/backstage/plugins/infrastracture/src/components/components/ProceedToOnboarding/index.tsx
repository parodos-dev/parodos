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

import React, { useContext, useEffect, useState } from 'react';
import {
  Box,
  Button,
  Checkbox,
  FormControlLabel, Grid,
  TextField,
  Typography,
} from '@material-ui/core';
import { useNavigate } from 'react-router-dom';
import { Flex } from 'rebass';
import * as R from 'ramda';
import ToastContext from '../../context/toast';
import useGetInfrastructurePlan from '../../hooks/useGetInfrastructurePlan';
import useSubmitInfrastructureRequest from '../../hooks/useSubmitInfrastructureRequest';
import useGetInfrastructureParams from '../../hooks/useGetInfrastructureParams';

const ProceedToOnboarding = ({
  scheduleSessionState,
  setScheduleSessionState,
  globalMigrationPlanState,
  setCurrentStepState,
  currentStepState,
  selectedOrganizationState,
  selectedRepoState,
  currentVersionState,
  upgradeState,
  migrateState,
  newState,
  setGlobalMigrationPlanState,
}) => {
  const getMigrationPlanHook = useGetInfrastructurePlan({
    selectedOrganizationState,
    selectedRepoState,
    currentVersionState,
    upgradeState,
    migrateState,
    newState,
  });
  const toastContext = useContext(ToastContext);
  const navigate = useNavigate();
  const postSubmitMigrationRequest = useSubmitInfrastructureRequest({
    selectedOrganizationState,
    selectedRepoState,
  });
  const artifactsToCreate = R.pathOr([], ['details'], globalMigrationPlanState);
  const prURL = R.pathOr([], ['prLink'], globalMigrationPlanState);
  const getInfrastructureParamsHook = useGetInfrastructureParams();
  const [params, setParams] = useState({});
  const [formValid, setFormValid] = useState(false);

  useEffect(() => {
    // getMigrationPlanHook.getMigrationPlan({ setGlobalMigrationPlanState });
    console.log(globalMigrationPlanState.workFlowId);
    getInfrastructureParamsHook.getInfrastructureParams({
      workflowName: globalMigrationPlanState.workFlowId,
    });
  }, []);

  const handleParamOnChange = (event, name) => {
    setFormValid(event.target.form.reportValidity());
    setParams({
      ...params,
      [name]: event.target.value,
    });
  };

  return (
    <div>
      <ul style={{ marginBottom: '25px' }}>
        {artifactsToCreate.map((artifact, index) => (
          <li style={{ marginBottom: '10px' }} key={index}>
            <div dangerouslySetInnerHTML={{ __html: artifact }} />
          </li>
        ))}
      </ul>
      <Typography paragraph>
        <b>Parameters</b>
      </Typography>
      <Box>
        <form>
          {getInfrastructureParamsHook.infrastructureParams.map(
            (param, index) => (
                <Box key={`param_${index}`}>
              <Grid item md={4}>
                <TextField
                  required={!param.optional}
                  style={{width: "100%", marginBottom: 20}}
                  id={`param_${index}`}
                  label={param.key}
                  helperText={param.description}
                  type={param.type.toLowerCase()}
                  onChange={() => handleParamOnChange(event, param.key)}
                />
                <span className="validity"></span>
              </Grid>
                </Box>
            ),
          )}
        </form>
      </Box>

      <Typography paragraph style={{ marginTop: 50 }}>
        <b>Next Steps</b>
      </Typography>
      <Typography paragraph>
        Please{' '}
        <span
          style={{ cursor: 'pointer' }}
          onClick={() => window.open(prURL, '_blank')}
        >
          review the artifacts that will be created
        </span>{' '}
        before proceeding and refer to your notifications panel for any
        recommended training
        <br />
        associated with this type of platform migration.
      </Typography>
      <FormControlLabel
        checked={scheduleSessionState}
        onChange={event => setScheduleSessionState(event.target.checked)}
        control={<Checkbox defaultChecked />}
        label="Schedule a support session to assist me with the new platform"
      />
      <Flex justifyContent="center" mt="100px">
        <Flex mr="20px">
          <Button
            onClick={() => setCurrentStepState(currentStepState - 1)}
            style={{
              width: '134px',
            }}
            variant="contained"
          >
            Previous
          </Button>
        </Flex>
        <Flex ml="20px">
          <Button
            style={{}}
            variant="contained"
            onClick={async () => {
              await postSubmitMigrationRequest.submitRequest({
                migrationPlan: globalMigrationPlanState,
                params: params,
              });
              setCurrentStepState(0);
              navigate('/deploy');
            }}
            disabled={postSubmitMigrationRequest.isLoading || !formValid}
          >
            Submit
          </Button>
        </Flex>
      </Flex>
    </div>
  );
};

export default ProceedToOnboarding;
