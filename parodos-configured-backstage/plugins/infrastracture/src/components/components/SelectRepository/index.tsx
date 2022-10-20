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
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import { Button, Card, IconButton } from '@material-ui/core';
import { Flex } from 'rebass';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import Avatar from '@material-ui/core/Avatar';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import GitHubIcon from '@material-ui/icons/GitHub';
import SearchInput from '../SearchInput';
import useGetRepositories from '../../hooks/useGetRepositories';

const SelectRepository = ({
  selectedRepoState,
  setSelectedRepoState,
  setCurrentStepState,
  currentStepState,
  selectedOrganizationState,
}) => {
  const getRepositoriesHook = useGetRepositories();
  const [searchTermState, setSearchTermState] = useState('');
  const [currentTabState, setCurrentTabState] = useState(0);
  const [currentListState, setCurrentListState] = useState([]);

  useEffect(() => {
    getRepositoriesHook.getRepositories({ orgName: selectedOrganizationState });
  }, []);

  useEffect(() => {
    if (!!getRepositoriesHook.repositories.length) {
      setCurrentListState(getRepositoriesHook.repositories);
    }
  }, [getRepositoriesHook.repositories]);

  useEffect(() => {
    if (getRepositoriesHook.repositories.length) {
      if (searchTermState) {
        const filteredList = getRepositoriesHook.repositories.filter(
          repository =>
            repository.toLowerCase().includes(searchTermState.toLowerCase()),
        );
        setCurrentListState(filteredList);
      } else {
        setCurrentListState(getRepositoriesHook.repositories);
      }
    }
  }, [searchTermState]);

  return (
    <div>
      <Flex justifyContent="flex-end">
        <SearchInput
          value={searchTermState}
          label="Search repositories"
          onChange={event => setSearchTermState(event.target.value)}
        />
      </Flex>
      <Tabs
        TabIndicatorProps={{ style: { background: '#00124F' } }}
        value={currentTabState}
        onChange={(_, index) => setCurrentTabState(index)}
      >
        <Tab
          label={`Repos available (${getRepositoriesHook.repositories.length})`}
          value={0}
        />
        <Tab label="Provisioned services (0)" value={1} />
      </Tabs>
      {currentTabState === 0 && (
        <List>
          {currentListState.map(repository => (
            <Card style={{ marginBottom: '10px' }} key={repository}>
              <ListItem
                selected={selectedRepoState === repository}
                onClick={() => setSelectedRepoState(repository)}
              >
                <ListItemAvatar>
                  <Avatar />
                </ListItemAvatar>
                <ListItemText
                  primary={
                    <Flex alignItems="center">
                      <Flex mr="10px">
                        <b>{`${selectedOrganizationState} / ${repository}`}</b>
                      </Flex>
                      <Flex mr="10px">{repository.description}</Flex>
                    </Flex>
                  }
                />
                <ListItemIcon>
                  <Flex alignItems="center">
                    <Flex mr="10px" style={{ fontSize: '14px' }}>
                      {repository.updatedAt}
                    </Flex>
                  </Flex>
                  <IconButton onClick={() => {}}>
                    <GitHubIcon />
                  </IconButton>
                </ListItemIcon>
              </ListItem>
            </Card>
          ))}
        </List>
      )}
      <Flex justifyContent="center" mt="100px">
        <Flex mr="20px">
          <Button
            onClick={() => {
              setCurrentStepState(currentStepState - 1);
              setSelectedRepoState('');
            }}
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
            disabled={!selectedRepoState}
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

export default SelectRepository;
