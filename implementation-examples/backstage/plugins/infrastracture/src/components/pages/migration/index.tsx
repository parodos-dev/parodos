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

import React, { useState } from 'react';
import {
  Button,
  Step,
  StepLabel,
  Stepper,
  Typography,
} from '@material-ui/core';

import SelectOrganization from '../../components/SelectOrganization';
import SelectRepository from '../../components/SelectRepository';
import SelectMigration from '../../components/SelectMigration';
import ReviewDetails from '../../components/ReviewDetails';
import ProceedToOnboarding from '../../components/ProceedToOnboarding';

const STEPS = [
  {
    text: 'Select an Organization',
  },
  {
    text: 'Select a Repository',
  },
  {
    text: 'Select Migration',
  },
  {
    text: 'Review Details',
  },
  {
    text: 'Submit',
  },
];

const Migration = () => {
  const [currentStepState, setCurrentStepState] = useState(0);
  const [selectedOrganizationState, setSelectedOrganizationState] =
    useState('');
  const [selectedRepoState, setSelectedRepoState] = useState('');
  const [pcfUpgradesState, setPcfUpgradesState] = useState('');
  const [newPlatformState, setNewPlatformState] = useState('');
  const [newVMState, setNewVMState] = useState('');
  const [currentVersionState, setCurrentVersionState] = useState('');
  const [scheduleSessionState, setScheduleSessionState] = useState(true);
  const [globalMigrationPlanState, setGlobalMigrationPlanState] = useState({});
  const [shouldShowMigrationTitleState, setShouldShowMigrationTitleState] =
    useState(true);

  const getTitleAndDescriptionForStep = () => {
    if (currentStepState === 0) {
      return {
        renderTitle: () => (
          <Typography variant="h5" paragraph>
            <b>Select an organization</b>
          </Typography>
        ),
        renderDescription: () => (
          <Typography paragraph>
            We found{' '}
            <b>
              the following organizations associated with your Employee ID.
              Select one to view your available repositories:
            </b>
          </Typography>
        ),
      };
    }
    if (currentStepState === 1) {
      return {
        renderTitle: () => (
          <Typography variant="h5" paragraph>
            <b>Select a repository</b>
          </Typography>
        ),
        renderDescription: () => (
          <div>
            <Typography>
              Parodos will assess your application for potential upgrades and
              migration.
            </Typography>
            <Button
              onClick={() => setCurrentStepState(0)}
              style={{
                textTransform: 'none',
                paddingLeft: 0,
              }}
            >
              Change organization
            </Button>
          </div>
        ),
      };
    }
    if (currentStepState === 2) {
      return {
        renderTitle: () =>
          shouldShowMigrationTitleState && (
            <Typography variant="h5" paragraph>
              <b>Select migration option</b>
            </Typography>
          ),
        renderDescription: () =>
          shouldShowMigrationTitleState && (
            <Typography paragraph>
              Your application qualifies for the following update/migration
              option:
            </Typography>
          ),
      };
    }
    if (currentStepState === 3) {
      return {
        renderTitle: () => (
          <Typography variant="h5" paragraph>
            <b>Review the details of your migration</b>
          </Typography>
        ),
        renderDescription: () => (
          <Typography paragraph>
            You have selected the following changes to your application:
          </Typography>
        ),
      };
    }
    if (currentStepState === 4) {
      return {
        renderTitle: () => (
          <Typography variant="h5" paragraph>
            <b>Ready for onboarding!</b>
          </Typography>
        ),
        renderDescription: () => (
          <Typography paragraph>
            New Platform Artifacts have been generated and checked into your
            Repo as{' '}
            <span
              style={{ cursor: 'pointer' }}
              onClick={() =>
                window.open(globalMigrationPlanState.prLink, '_blank')
              }
            >
              {globalMigrationPlanState.prLink}:
            </span>
          </Typography>
        ),
      };
    }
    return {
      renderTitle: () => {},
      renderDescription: () => {},
    };
  };

  const renderContent = () => {
    if (currentStepState === 0) {
      return (
        <SelectOrganization
          selectedOrganizationState={selectedOrganizationState}
          setSelectedOrganizationState={setSelectedOrganizationState}
          currentStepState={currentStepState}
          setCurrentStepState={setCurrentStepState}
        />
      );
    }
    if (currentStepState === 1) {
      return (
        <SelectRepository
          selectedRepoState={selectedRepoState}
          setSelectedRepoState={setSelectedRepoState}
          currentStepState={currentStepState}
          setCurrentStepState={setCurrentStepState}
          selectedOrganizationState={selectedOrganizationState}
        />
      );
    }
    if (currentStepState === 2) {
      return (
        <SelectMigration
          currentStepState={currentStepState}
          setCurrentStepState={setCurrentStepState}
          pcfUpgradesState={pcfUpgradesState}
          setPcfUpgradesState={setPcfUpgradesState}
          newPlatformState={newPlatformState}
          setNewPlatformState={setNewPlatformState}
          newVMState={newVMState}
          setNewVMState={setNewVMState}
          setCurrentVersionState={setCurrentVersionState}
          setShouldShowMigrationTitleState={setShouldShowMigrationTitleState}
          selectedOrganizationState={selectedOrganizationState}
          selectedRepoState={selectedRepoState}
        />
      );
    }
    if (currentStepState === 3) {
      return (
        <ReviewDetails
          setCurrentStepState={setCurrentStepState}
          currentStepState={currentStepState}
          scheduleSessionState={scheduleSessionState}
          selectedOrganizationState={selectedOrganizationState}
          selectedRepoState={selectedRepoState}
          currentVersionState={currentVersionState}
          pcfUpgradesState={pcfUpgradesState}
          newPlatformState={newPlatformState}
          newVMState={newVMState}
          setGlobalMigrationPlanState={setGlobalMigrationPlanState}
        />
      );
    }
    if (currentStepState === 4) {
      return (
        <ProceedToOnboarding
          setScheduleSessionState={setScheduleSessionState}
          globalMigrationPlanState={globalMigrationPlanState}
          currentStepState={currentStepState}
          setCurrentStepState={setCurrentStepState}
          selectedOrganizationState={selectedOrganizationState}
          selectedRepoState={selectedRepoState}
          currentVersionState={currentVersionState}
          pcfUpgradesState={pcfUpgradesState}
          newPlatformState={newPlatformState}
          newVMState={newVMState}
          setGlobalMigrationPlanState={setGlobalMigrationPlanState}
        />
      );
    }
  };

  const renderTitleAndDescriptionFunctions = getTitleAndDescriptionForStep();

  return (
    <div>
      <Typography
        style={{ fontSize: '32px', color: '#00124F' }}
        variant="h4"
        paragraph
      >
        <b>Project Assessment</b>
      </Typography>
      <Typography paragraph>
        Assess your application for potential upgrades and migrations
      </Typography>
      <Stepper
        style={{ marginBottom: '30px' }}
        alternativeLabel
        activeStep={currentStepState}
      >
        {STEPS.map((step, index) => (
          <Step key={step.text}>
            <StepLabel color={'secondary'}>{step.text}</StepLabel>
          </Step>
        ))}
      </Stepper>
      <div style={{ padding: '0 70px' }}>
        <div style={{ marginBottom: '35px' }}>
          {renderTitleAndDescriptionFunctions.renderTitle()}
          {renderTitleAndDescriptionFunctions.renderDescription()}
        </div>
        {renderContent()}
      </div>
    </div>
  );
};

export default Migration;
