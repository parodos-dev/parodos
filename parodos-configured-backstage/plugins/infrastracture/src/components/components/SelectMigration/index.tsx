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

import React, { useEffect } from 'react';
import * as R from 'ramda';
import { Flex } from 'rebass';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormGroup from '@material-ui/core/FormGroup';
import Radio from '@material-ui/core/Radio';
import { Button, Grid } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import useGetMigrationOptions from '../../hooks/useGetMigrationOptions';

const SelectMigration = ({
  currentStepState,
  setCurrentStepState,
  pcfUpgradesState,
  setPcfUpgradesState,
  newPlatformState,
  setNewPlatformState,
  newVMState,
  setNewVMState,
  setCurrentVersionState,
  setShouldShowMigrationTitleState,
  selectedOrganizationState,
  selectedRepoState,
}) => {
  const getMigrationOptions = useGetMigrationOptions();

  useEffect(() => {
    getMigrationOptions.getMigrationOptions({
      setShouldShowMigrationTitleState,
      orgName: selectedOrganizationState,
      repo: selectedRepoState,
    });
  }, []);

  const optionsAvailable = R.pathOr(
    '',
    ['migrationOptions', 'optionsAvailable'],
    getMigrationOptions,
  );
  const currentPlatform = R.pathOr(
    '',
    ['migrationOptions', 'platformName'],
    getMigrationOptions,
  );
  const currentVersion = R.pathOr(
    '',
    ['migrationOptions', 'currentVersion'],
    getMigrationOptions,
  );
  const upgradeOptions = R.pathOr(
    [],
    ['migrationOptions', 'upgradeOptions'],
    getMigrationOptions,
  );
  const migrationOptions = R.pathOr(
    [],
    ['migrationOptions', 'migrationPlatform'],
    getMigrationOptions,
  );
  const vmUpdateOptions = R.pathOr(
    [],
    ['migrationOptions', 'vmUpdateOptions'],
    getMigrationOptions,
  );

  useEffect(() => {
    if (currentVersion && !newPlatformState && !newVMState) {
      setPcfUpgradesState(currentVersion);
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
                  <FormControlLabel
                    defaultChecked
                    onChange={event => {
                      setNewPlatformState(null);
                      setPcfUpgradesState(event.target.value);
                      setNewVMState(null);
                    }}
                    checked={pcfUpgradesState === currentVersion}
                    value={currentVersion}
                    control={<Radio />}
                    label={`${currentVersion} (Current)`}
                  />
                  {upgradeOptions.map(upgradeOption => (
                    <FormControlLabel
                      onChange={event => {
                        setNewPlatformState(null);
                        setPcfUpgradesState(event.target.value);
                        setNewVMState(null);
                      }}
                      key={upgradeOption}
                      checked={pcfUpgradesState === upgradeOption}
                      value={upgradeOption}
                      control={<Radio />}
                      label={upgradeOption}
                    />
                  ))}
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
                  {vmUpdateOptions?.length == 0 ? (
                    <Typography variant={'body1'}>N/A</Typography>
                  ) : (
                    vmUpdateOptions.map(vmUpdateOption => (
                      <FormControlLabel
                        key={vmUpdateOption}
                        onChange={event => {
                          setPcfUpgradesState(null);
                          setNewPlatformState(null);
                          setNewVMState(event.target.value);
                        }}
                        checked={newVMState === vmUpdateOption}
                        value={vmUpdateOption}
                        control={<Radio />}
                        label={vmUpdateOption}
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
                  {migrationOptions.map(migrationOption => (
                    <FormControlLabel
                      key={migrationOption}
                      onChange={event => {
                        setPcfUpgradesState(null);
                        setNewPlatformState(event.target.value);
                        setNewVMState(null);
                      }}
                      checked={newPlatformState === migrationOption}
                      value={migrationOption}
                      control={<Radio />}
                      label={migrationOption}
                    />
                  ))}
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
          <Grid
            container
            style={{ marginTop: '20px', marginLeft: '50px' }}
            justifyContent={'left'}
          >
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
        {optionsAvailable && (
          <Flex ml="20px">
            <Button
              style={{
                width: '134px',
              }}
              variant="contained"
              onClick={() => setCurrentStepState(currentStepState + 1)}
            >
              Next
            </Button>
          </Flex>
        )}
      </Flex>
    </div>
  );
};

export default SelectMigration;
