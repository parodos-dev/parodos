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

import * as React from 'react';
import { useContext } from 'react';
import PipelineStep from './PipelineStep';
import { RunDetailContext } from '../../../../../../../contexts/projects/runDetail';
import makeStyles from '@material-ui/core/styles/makeStyles';
import { Grid, Typography } from '@material-ui/core';

const useStyles = makeStyles(theme => ({
  root: {},
}));
export default function PipelineStepper() {
  const classes = useStyles();
  const runDetailContext = useContext(RunDetailContext);
  const pipelineStages = runDetailContext.pipeline.pipelineStages;
  const lastIndex =
    pipelineStages.filter(
      stage =>
        sessionStorage.getItem('user_name') === 'lshannon' ||
        stage.stageName !== 'prod',
    ).length - 1;
  return (
    <Grid container>
      <Grid container>
        <Typography variant={'subtitle1'}>DEPLOYMENT STAGES</Typography>
      </Grid>
      <Grid
        container
        style={{ marginTop: 3, flexDirection: 'row', display: 'flex' }}
      >
        {pipelineStages
          .filter(
            stage =>
              sessionStorage.getItem('user_name') === 'lshannon' ||
              stage.stageName !== 'prod',
          )
          .map((stage, index) => {
            return (
              <PipelineStep
                key={index}
                stage={stage}
                isLast={index === lastIndex}
              />
            );
          })}
      </Grid>
    </Grid>
  );
}
