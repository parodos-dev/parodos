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

import React, { useContext, useEffect, useState } from 'react';
import { Box, Button, Grid, TextField, Typography } from '@material-ui/core';
import Autocomplete from '@material-ui/lab/Autocomplete';
import useGetAllProjects from '../hooks/useGetAllProjects';
import { ProjectsContext } from '../contexts/projects/projects';
import SearchIcon from '@material-ui/icons/Search';
import OverviewList from '../components/overview/overviewList';

const Overview = () => {
  const [selectedProjects, setSelectedProjects] = useState([]);
  const getAllProjectsHook = useGetAllProjects();
  const projectsContext = useContext(ProjectsContext);
  const searchOptions = [
    'All active projects',
    'last 7 days',
    'last 14 days',
    'last 30 days',
    'Archived',
  ];
  useEffect(() => {
    getAllProjectsHook.getAllProjects().then();
  }, [projectsContext.allProjects.length, selectedProjects.length]);

  const handleSelect = (select: any, project: any) => {
    if (select) {
      let list = selectedProjects.concat(project);
      setSelectedProjects(list);
    } else {
      let list = selectedProjects.filter(p => project !== p);
      setSelectedProjects(list);
    }
  };

  return (
    <Grid container spacing={2} height={10} px={10} my={3}>
      <Grid item xs={2}>
        <Typography variant="h4" paragraph>
          <b>Projects({projectsContext.allProjects.length})</b>
        </Typography>
      </Grid>
      <Grid item xs={2}>
        <Grid container>
          <Grid item xs={11}>
            <Autocomplete
              id="search-project"
              options={searchOptions}
              renderInput={params => (
                <TextField {...params} variant="standard" />
              )}
              value={searchOptions[0]}
            />
          </Grid>
          <Grid item xs={1} style={{ alignSelf: 'center' }}>
            <SearchIcon fontSize="small" style={{ marginTop: 5 }} />
          </Grid>
        </Grid>
      </Grid>
      <Grid item xs={6}>
        {selectedProjects.length > 0 && (
          <Grid container>
            <Grid item xs={10} pr={3} pt={1}>
              <Typography variant="body1" paragraph textAlign={'right'}>
                <b>{selectedProjects.length} project(s) selected</b>
              </Typography>
            </Grid>
            <Grid item xs={2} display={'flex'} justifyContent={'flex-start'}>
              <Button variant={'outlined'} color={'secondary'}>
                ARCHIVE
              </Button>
            </Grid>
          </Grid>
        )}
      </Grid>
      <Grid container mx={4}>
        <OverviewList handleSelect={handleSelect} />
      </Grid>
    </Grid>
  );
};

export default Overview;
