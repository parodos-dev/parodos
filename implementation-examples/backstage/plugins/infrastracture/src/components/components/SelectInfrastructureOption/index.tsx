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
import * as R from 'ramda';
import { Flex } from 'rebass';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormGroup from '@material-ui/core/FormGroup';
import Radio from '@material-ui/core/Radio';
import { Button, Grid } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import useGetInfrastructureOptions from '../../hooks/useGetInfrastructureOptions';

const SelectInfrastructureOption = ({
  firstStep,
  assessmentParams,
  currentStepState,
  setCurrentStepState,
  upgradeState,
  setUpgradeState,
  migrateState,
  setMigrateState,
  newState,
  setNewState,
  setCurrentVersionState,
  setShouldShowMigrationTitleState,
  selectedOrganizationState,
  selectedRepoState,
  globalMigrationPlanState,
  setGlobalMigrationPlanState,
}) => {
  const getMigrationOptions = useGetInfrastructureOptions();
  const [currentVersion, setCurrentVersion] = useState(false);
  const [upgradeOptions, setUpgradeOptions] = useState([]);
  const [migrationOptions, setMigrationOptions] = useState([]);
  const [newOptions, setNewOptions] = useState([]);
  const [optionsAvailable, setOptionsAvailable] = useState([]);

  useEffect(() => {
    getMigrationOptions
      .getInfrastructureOptions(assessmentParams)
      .then(data => {
        const current = R.pathOr(false, ['currentVersion'], data);
        const upgrade = R.pathOr([], ['upgradeOptions'], data);
        const migrate = R.pathOr([], ['migrationOptions'], data);
        const newOpt = R.pathOr([], ['newOptions'], data);
        setCurrentVersion(current);
        setUpgradeOptions(upgrade);
        setMigrationOptions(migrate);
        setNewOptions(newOpt);
        setOptionsAvailable(newOpt || upgrade || migrate);
        setShouldShowMigrationTitleState(newOpt || upgrade || migrate);
      });
  }, []);
  useEffect(() => {
    if (currentVersion && !migrateState && !newState) {
      setUpgradeState(currentVersion);
      setCurrentVersionState(currentVersion);
    }
  }, [currentVersion]);

  return (
    <div>
      {optionsAvailable ? (
        <Flex flex={2} style={{ width: '755px' }}>
          <Flex flex={1} flexDirection="column" mr="20px">
            <Typography paragraph>
              <b>{`Current Environment`}</b>
            </Typography>
            <Card>
              <CardContent>
                <FormGroup>
                  {currentVersion ? (
                    <FormControlLabel
                      defaultChecked
                      onChange={event => {
                        setMigrateState(null);
                        setUpgradeState(currentVersion);
                        setNewState(null);
                        setGlobalMigrationPlanState(currentVersion);
                      }}
                      checked={
                        globalMigrationPlanState.displayName ===
                        currentVersion.displayName
                      }
                      value={currentVersion.displayName}
                      control={<Radio />}
                      label={`${currentVersion.displayName} (Current)`}
                    />
                  ) : (
                    <Typography variant={'body1'}>N/A</Typography>
                  )}
                </FormGroup>
              </CardContent>
            </Card>
          </Flex>
          <Flex flex={1} flexDirection="column" ml="20px">
            <Typography paragraph>
              <b>Upgrade</b>
            </Typography>
            <Card>
              <CardContent>
                <FormGroup>
                  {upgradeOptions.length == 0 ? (
                    <Typography variant={'body1'}>N/A</Typography>
                  ) : (
                    upgradeOptions.map(upgradeOption => (
                      <FormControlLabel
                        onChange={event => {
                          setUpgradeState(upgradeOption);
                          setMigrateState(null);
                          setNewState(null);
                          setGlobalMigrationPlanState(upgradeOption);
                        }}
                        key={upgradeOption.displayName}
                        checked={
                          globalMigrationPlanState.displayName ===
                          upgradeOption.displayName
                        }
                        value={upgradeOption.displayName}
                        control={<Radio />}
                        label={upgradeOption.displayName}
                      />
                    ))
                  )}
                </FormGroup>
              </CardContent>
            </Card>
          </Flex>
          <Flex flex={1} flexDirection="column" ml="20px">
            <Typography paragraph>
              <b>New Platform</b>
            </Typography>
            <Card>
              <CardContent>
                <FormGroup>
                  {newOptions.length == 0 ? (
                    <Typography variant={'body1'}>N/A</Typography>
                  ) : (
                    newOptions.map(newOption => (
                      <FormControlLabel
                        key={newOption.displayName}
                        onChange={event => {
                          setUpgradeState(null);
                          setMigrateState(null);
                          setNewState(newOption);
                          setGlobalMigrationPlanState(newOption);
                        }}
                        checked={
                          globalMigrationPlanState.displayName ===
                          newOption.displayName
                        }
                        value={newOption.displayName}
                        control={<Radio />}
                        label={newOption.displayName}
                      />
                    ))
                  )}
                </FormGroup>
              </CardContent>
            </Card>
          </Flex>
          <Flex flex={1} flexDirection="column" ml="20px">
            <Typography paragraph>
              <b>Migrate</b>
            </Typography>
            <Card>
              <CardContent>
                <FormGroup>
                  {migrationOptions.length == 0 ? (
                    <Typography variant={'body1'}>N/A</Typography>
                  ) : (
                    migrationOptions.map(migrationOption => (
                      <FormControlLabel
                        key={migrationOption.displayName}
                        onChange={event => {
                          setUpgradeState(null);
                          setMigrateState(migrationOption);
                          setNewState(null);
                          setGlobalMigrationPlanState(migrationOption);
                        }}
                        checked={
                          globalMigrationPlanState.displayName ===
                          migrationOption
                        }
                        value={migrationOption.displayName}
                        control={<Radio />}
                        label={migrationOption.displayName}
                      />
                    ))
                  )}
                </FormGroup>
              </CardContent>
            </Card>
          </Flex>
        </Flex>
      ) : (
        <div>
          <Typography align="center" variant="subtitle1" paragraph>
            <b>No migration options were found for this repo</b>
          </Typography>
          <Grid container justifyContent={'center'}>
            {/*<Button size="large" variant="contained">*/}
            {/*  Create a Ticket*/}
            {/*</Button>*/}
          </Grid>
          <Grid container style={{ marginTop: '20px', marginLeft: '50px' }}>
            <Typography align="center" variant="h5" paragraph>
              <span>
                - We have assessed your project and it violates Factor 3 of the
                12 factor principles (https://12factor.net/config) by{' '}
              </span>
            </Typography>
          </Grid>
          <Grid
            container
            style={{ marginTop: '20px', marginLeft: '50px' }}
            justifyContent={'left'}
          >
            <Typography align="center" variant="h5" paragraph>
              <span>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- requiring the
                OS to be configured to store application details prior to the
                application starting.
              </span>
            </Typography>
          </Grid>
          <Grid
            container
            style={{ marginTop: '20px', marginLeft: '50px' }}
            justifyContent={'left'}
          >
            <Typography align="center" variant="h5" paragraph>
              <span>
                - Check the training tab to see some training that can help you
                and your team resolve this. Complete the necessary training and
                then try to migrate again
              </span>
            </Typography>
          </Grid>
        </div>
      )}
      <Flex justifyContent="center" mt="100px">
        {firstStep === 0 && (
          <Flex mr={optionsAvailable ? '20px' : 0}>
            <Button
              onClick={() =>
                optionsAvailable
                  ? setCurrentStepState(currentStepState - 1)
                  : setCurrentStepState(0)
              }
              style={{
                width: '134px',
              }}
              variant="contained"
            >
              {optionsAvailable ? 'Previous' : 'Complete'}
            </Button>
          </Flex>
        )}
        {optionsAvailable && (
          <Flex ml="20px">
            <Button
              style={{
                width: '134px',
              }}
              variant="contained"
              onClick={() => setCurrentStepState(currentStepState + 1)}
              disabled={!upgradeState && !migrateState && !newState}
            >
              Next
            </Button>
          </Flex>
        )}
      </Flex>
    </div>
  );
};

export default SelectInfrastructureOption;
