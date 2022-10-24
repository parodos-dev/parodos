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
import { Flex } from 'rebass';
import {
  Button,
  Card,
  DialogContent,
  IconButton,
  Typography,
} from '@material-ui/core';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import Avatar from '@material-ui/core/Avatar';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import GitHubIcon from '@material-ui/icons/GitHub';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import SearchInput from '../SearchInput';
import useGetOrganizations from '../../hooks/useGetOrganizations';

const SelectOrganization = ({
  selectedOrganizationState,
  setSelectedOrganizationState,
  currentStepState,
  setCurrentStepState,
}) => {
  const [noOrganizationFoundModalState, setNoOrganizationFoundModalState] =
    useState(false);
  const getOrganizationsHook = useGetOrganizations();
  const [searchTermState, setSearchTermState] = useState('');
  const [currentListState, setCurrentListState] = useState([]);

  useEffect(() => {
    getOrganizationsHook.getOrganizations();
  }, []);

  useEffect(() => {
    if (!!getOrganizationsHook.organizations.length) {
      setCurrentListState(getOrganizationsHook.organizations);
    }
  }, [getOrganizationsHook.organizations]);

  useEffect(() => {
    if (getOrganizationsHook.organizations.length) {
      if (searchTermState) {
        const filteredList = getOrganizationsHook.organizations.filter(
          organization =>
            organization.toLowerCase().includes(searchTermState.toLowerCase()),
        );
        setCurrentListState(filteredList);
      } else {
        setCurrentListState(getOrganizationsHook.organizations);
      }
    }
  }, [searchTermState]);

  return (
    <div>
      <Flex justifyContent="space-between">
        <Button
          onClick={() => setNoOrganizationFoundModalState(true)}
          style={{ textTransform: 'none' }}
        >
          <Typography variant="body1" paragraph>
            Don't see your organization?
          </Typography>
        </Button>
        <Dialog
          open={noOrganizationFoundModalState}
          onClose={() => setNoOrganizationFoundModalState(false)}
        >
          <DialogTitle>Don't see your organization?</DialogTitle>
          <DialogContent>
            <Flex justifyContent="center">
              <Button variant="contained">Create a Ticket</Button>
            </Flex>
          </DialogContent>
        </Dialog>
        <SearchInput
          value={searchTermState}
          label="Search organizations"
          onChange={event => setSearchTermState(event.target.value)}
        />
      </Flex>
      <List>
        {currentListState.map(organization => (
          <Card style={{ marginBottom: '10px' }} key={organization}>
            <ListItem
              button
              selected={selectedOrganizationState === organization}
              onClick={() => setSelectedOrganizationState(organization)}
            >
              <ListItemAvatar>
                <Avatar />
              </ListItemAvatar>
              <ListItemText
                primary={
                  <Flex alignItems="center">
                    <Flex>
                      <b>{organization}</b>
                    </Flex>
                  </Flex>
                }
              />
              <ListItemIcon>
                <IconButton onClick={() => {}}>
                  <GitHubIcon />
                </IconButton>
              </ListItemIcon>
            </ListItem>
          </Card>
        ))}
      </List>
      <Flex justifyContent="center" mt="100px">
        <Button
          disabled={!selectedOrganizationState}
          style={{
            width: '134px',
          }}
          variant="contained"
          onClick={() => setCurrentStepState(currentStepState + 1)}
        >
          Next
        </Button>
      </Flex>
    </div>
  );
};

export default SelectOrganization;
