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
import { IProjectOverview } from '../../interface/pipelineOverview';
import { Box, Grid, Link, Select, Typography } from '@material-ui/core';
import { StyledChip, useStyles } from './styles';
import useGetProjectOverview from '../../hooks/useGetProjectOverview';
import useGetBranchesOverview from '../../hooks/useGetBranchesOverview';

const AccordionDetail = ({ project }) => {
  const [pipelineOverview, setPipelineOverview] = useState<
    IProjectOverview | any
  >({ stages: [] });
  const [branches, setBranches] = useState<string[] | any>([]);
  const classes = useStyles();
  const getProjectOverview = useGetProjectOverview();
  const getBranches = useGetBranchesOverview();

  useEffect(() => {
    getBranches.getBranches(project).then(branches => {
      setBranches(branches);
      getProjectOverview
        .getOverview(project.id, branches[0])
        .then(projectOverview => setPipelineOverview(projectOverview));
    });
  }, []);

  const handleChange = (event: { target: { value: any } }) => {
    getProjectOverview
      .getOverview(project.id, event.target.value)
      .then(projectOverview => setPipelineOverview(projectOverview));
  };

  return (
    <Box width={'100%'}>
      <Grid
        container
        justifyContent="center"
        spacing={0}
        style={{ height: 30 }}
      >
        <Grid item xs={2}>
          <Typography variant={'subtitle2'} color={'textSecondary'}>
            BRANCH
          </Typography>
        </Grid>
        <Grid item xs={2}>
          <Typography variant={'subtitle2'} color={'textSecondary'}>
            ARTIFACT
          </Typography>
        </Grid>
        <Grid item xs={2}>
          <Typography variant={'subtitle2'} color={'textSecondary'}>
            DEPLOYMENTS
          </Typography>
        </Grid>
        <Grid item xs={6}>
          <Link
            href={pipelineOverview.latestCommitUrl}
            target="_blanks"
            color="inherit"
            underline="always"
          >
            <Typography
              variant={'body1'}
              color={'textSecondary'}
              style={{ marginLeft: 20 }}
            >
              View More Details
            </Typography>
          </Link>
        </Grid>
      </Grid>
      <Grid container spacing={0}>
        <Grid item xs={2}>
          <Select native defaultValue={branches[0]} onChange={handleChange}>
            {branches.map((b: string | any) => (
              <option value={b} key={b}>
                {b}
              </option>
            ))}
          </Select>
        </Grid>
        <Grid item container xs={10}>
          <Grid item xs={1}>
            <StyledChip
              label={pipelineOverview.latestCommitId}
              color={'secondary'}
            ></StyledChip>
          </Grid>
          <Grid item xs={1}>
            <hr className={classes.hr} />
          </Grid>
          <Grid container item xs={10}>
            {pipelineOverview.stages.map((stage, index) => (
              <Grid container item xs={3} key={index}>
                <Grid
                  item
                  xs={6}
                  style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                  }}
                >
                  <StyledChip
                    label={`${stage.stageName}(${stage.builds})`}
                    variant={stage.currentStage ? 'default' : 'outlined'}
                    color={stage.currentStage ? 'primary' : 'default'}
                  ></StyledChip>
                </Grid>
                {index !== pipelineOverview.stages.length - 1 && (
                  <Grid item xs={6}>
                    <hr className={classes.hr} />
                  </Grid>
                )}
              </Grid>
            ))}
          </Grid>
        </Grid>
      </Grid>
    </Box>
  );
};

export default AccordionDetail;
