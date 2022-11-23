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

import React, { useEffect, useState } from 'react';
import {
  Button,
  Step,
  StepLabel,
  Stepper,
  Typography,
} from '@material-ui/core';

import SelectOrganization from '../../components/SelectOrganization';
import SelectRepository from '../../components/SelectRepository';
import SelectInfrastructureOption from '../../components/SelectInfrastructureOption';
import ReviewDetails from '../../components/ReviewDetails';
import Submission from '../../components/Submission';
import AssessmentDetails from '../../components/AssessmentDetails';

const STEPS = [
  {
    text: 'Application Assessment',
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

const Infrastructure = () => {
  const [currentStepState, setCurrentStepState] = useState(0);
  const [selectedOrganizationState, setSelectedOrganizationState] =
    useState('');
  const [selectedRepoState, setSelectedRepoState] = useState('');
  const [upgradeState, setUpgradeState] = useState('');
  const [migrateState, setMigrateState] = useState('');
  const [newState, setNewState] = useState('');
  const [currentVersionState, setCurrentVersionState] = useState('');
  const [scheduleSessionState, setScheduleSessionState] = useState(true);
  const [globalMigrationPlanState, setGlobalMigrationPlanState] = useState({});
  const [shouldShowMigrationTitleState, setShouldShowMigrationTitleState] =
    useState(true);
  const [assessmentParams, setAssessmentParams] = useState({});
  const [firstStep, setFirstStep] = useState(0);

  useEffect(() => {}, [firstStep]);

  const getTitleAndDescriptionForStep = () => {
    if (currentStepState === 0) {
      return {
        renderTitle: () => (
          <Typography variant="h5" paragraph>
            <b>Provide Assessment Parameters</b>
          </Typography>
        ),
        renderDescription: () => (
          <Typography paragraph>
            <b>please provide parameter(s) for Assessment work flow:</b>
          </Typography>
        ),
      };
    }
    if (currentStepState === 1) {
      return {
        renderTitle: () =>
          shouldShowMigrationTitleState && (
            <Typography variant="h5" paragraph>
              <b>Select infrastructure option</b>
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
    if (currentStepState === 2) {
      return {
        renderTitle: () => (
          <Typography variant="h5" paragraph>
            <b>Review the details of your request</b>
          </Typography>
        ),
        renderDescription: () => (
          <Typography paragraph>
            You have selected the following changes to your application:
          </Typography>
        ),
      };
    }
    if (currentStepState === 3) {
      return {
        renderTitle: () => (
          <Typography variant="h5" paragraph>
            <b>Ready for proceeding!</b>
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
        <AssessmentDetails
          setAssessmentParams={setAssessmentParams}
          setCurrentStepState={setCurrentStepState}
          setFirstStep={setFirstStep}
        />
      );
    }
    if (currentStepState === 1) {
      return (
        <SelectInfrastructureOption
          firstStep={firstStep}
          assessmentParams={assessmentParams}
          currentStepState={currentStepState}
          setCurrentStepState={setCurrentStepState}
          upgradeState={upgradeState}
          setUpgradeState={setUpgradeState}
          migrateState={migrateState}
          setMigrateState={setMigrateState}
          newState={newState}
          setNewState={setNewState}
          setCurrentVersionState={setCurrentVersionState}
          setShouldShowMigrationTitleState={setShouldShowMigrationTitleState}
          selectedOrganizationState={selectedOrganizationState}
          selectedRepoState={selectedRepoState}
          globalMigrationPlanState={globalMigrationPlanState}
          setGlobalMigrationPlanState={setGlobalMigrationPlanState}
        />
      );
    }
    if (currentStepState === 2) {
      return (
        <ReviewDetails
          setCurrentStepState={setCurrentStepState}
          currentStepState={currentStepState}
          selectedOrganizationState={selectedOrganizationState}
          selectedRepoState={selectedRepoState}
          currentVersionState={currentVersionState}
          upgradeState={upgradeState}
          migrateState={migrateState}
          newState={newState}
        />
      );
    }
    if (currentStepState === 3) {
      return (
        <Submission
          assessmentParams={assessmentParams}
          scheduleSessionState={scheduleSessionState}
          setScheduleSessionState={setScheduleSessionState}
          globalMigrationPlanState={globalMigrationPlanState}
          currentStepState={currentStepState}
          setCurrentStepState={setCurrentStepState}
          selectedOrganizationState={selectedOrganizationState}
          selectedRepoState={selectedRepoState}
          currentVersionState={currentVersionState}
          upgradeState={upgradeState}
          migrateState={migrateState}
          newState={newState}
          setGlobalMigrationPlanState={setGlobalMigrationPlanState}
        />
      );
    } else return null;
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
        activeStep={currentStepState - firstStep}
      >
        {STEPS.slice(firstStep).map((step, index) => (
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

export default Infrastructure;
