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

import React, { useContext, useEffect } from 'react';
import {
  Button,
  Checkbox,
  FormControlLabel,
  Typography,
} from '@material-ui/core';
import { useNavigate } from 'react-router-dom';
import { Flex } from 'rebass';
import * as R from 'ramda';
import ToastContext from '../../context/toast';
import useGetMigrationPlan from '../../hooks/useGetMigrationPlan';
import useSubmitMigrationRequest from '../../hooks/useSubmitMigrationRequest';

const ProceedToOnboarding = ({
  scheduleSessionState,
  setScheduleSessionState,
  globalMigrationPlanState,
  setCurrentStepState,
  currentStepState,
  selectedOrganizationState,
  selectedRepoState,
  currentVersionState,
  pcfUpgradesState,
  newPlatformState,
  newVMState,
  setGlobalMigrationPlanState,
}) => {
  const getMigrationPlanHook = useGetMigrationPlan({
    selectedOrganizationState,
    selectedRepoState,
    currentVersionState,
    pcfUpgradesState,
    newPlatformState,
    newVMState,
  });
  const toastContext = useContext(ToastContext);
  const navigate = useNavigate();
  const postSubmitMigrationRequest = useSubmitMigrationRequest({
    selectedOrganizationState,
    selectedRepoState,
  });
  const artifactsToCreate = R.pathOr(
    [],
    ['artifactsToCreate'],
    globalMigrationPlanState,
  );
  const prURL = R.pathOr([], ['prLink'], globalMigrationPlanState);

  useEffect(() => {
    getMigrationPlanHook.getMigrationPlan({ setGlobalMigrationPlanState });
  }, []);

  return (
    <div>
      <ul style={{ marginBottom: '25px' }}>
        {artifactsToCreate.map(artifact => (
          <li style={{ marginBottom: '10px' }} key={artifact}>
            <div dangerouslySetInnerHTML={{ __html: artifact }} />
          </li>
        ))}
      </ul>
      <Typography paragraph>
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
              });
              setCurrentStepState(0);
              toastContext.handleOpenToast(
                `Transitioning your session to onboarding flow...`,
                'success',
              );
              navigate('/deploy');
            }}
            disabled={postSubmitMigrationRequest.isLoading}
          >
            Submit
          </Button>
        </Flex>
      </Flex>
    </div>
  );
};

export default ProceedToOnboarding;
