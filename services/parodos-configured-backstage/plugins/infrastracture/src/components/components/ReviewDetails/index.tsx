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

import React from 'react';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import Avatar from '@material-ui/core/Avatar';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import { Button, Card, Typography } from '@material-ui/core';
import { Flex } from 'rebass';

const ReviewDetails = ({
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
  const getLabelForMigration = () => {
    if (!upgradeState && !migrateState && !newState) {
      return '';
    }
    if (!!upgradeState) {
      if (currentVersionState === upgradeState) {
        return `Keeping ${currentVersionState.displayName}`;
      }
      return `Migrating from ${currentVersionState.displayName} to ${upgradeState.displayName}`;
    } else if (newState) {
      return `${newState.displayName}`;
    }
    return `Migrating from ${currentVersionState.displayName} to ${migrateState.displayName}`;
  };

  return (
    <div>
      <Card style={{ marginBottom: '35px' }}>
        <ListItem selected>
          <ListItemAvatar>
            <Avatar />
          </ListItemAvatar>
          <ListItemText
            primary={
              <b>{`${selectedOrganizationState} / ${selectedRepoState}`}</b>
            }
          />
          <ListItemText primary={<div>{getLabelForMigration()}</div>} />
        </ListItem>
      </Card>
      <Typography paragraph>
        <b>Confirmation</b>
      </Typography>
      <Typography paragraph>
        By clicking Next you will be confirming that this action is correct.
      </Typography>
      <Typography>
        Parodos will create the required files and direct you to complete the
        Onboarding process.
      </Typography>
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
            style={{
              width: '134px',
            }}
            variant="contained"
            onClick={() => setCurrentStepState(currentStepState + 1)}
          >
            Next
          </Button>
        </Flex>
      </Flex>
    </div>
  );
};

export default ReviewDetails;
