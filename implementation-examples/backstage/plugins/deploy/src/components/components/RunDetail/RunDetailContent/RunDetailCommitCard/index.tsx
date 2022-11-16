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

import React, { useContext } from 'react';
import { useStyles } from './styles';
import clsx from 'clsx';
import { Grid, Typography } from '@material-ui/core';
import { RunDetailContext } from '../../../../contexts/projects/runDetail';
import { InstructionsIcon } from '../../../common/icon/InstructionsIcon';

const getPrePipelineMsg = (classes, state) => {
  console.log('state: ' + state);
  switch (state) {
    case 'error':
      return <Typography className={classes.preErr}>Build failed</Typography>;
    case 'active':
      return (
        <Typography className={classes.pre}>Build in progress . . .</Typography>
      );
    default:
      return <Typography variant={'body1'}>nothing to show</Typography>;
  }
};

export default function RunDetailCommitCard() {
  const runDetailContext = useContext(RunDetailContext);
  const classes = useStyles();

  return runDetailContext.pipeline?.activeStageName === 'pre' ? (
    getPrePipelineMsg(
      classes,
      runDetailContext.pipeline?.pipelineState?.activeStageState,
    )
  ) : (
    <Grid
      container
      className={clsx(
        classes.root,
        runDetailContext.pipeline?.pipelineState?.activeStageState ===
          'error' && classes.errRoot,
      )}
      alignItems="center"
    >
      <Grid container display="flex" mr={2}>
        <InstructionsIcon className={classes.icon} />
        <Typography variant={'body1'} className={classes.commitId} ml={1}>
          {runDetailContext.pipeline?.latestCommitId}
        </Typography>
      </Grid>
    </Grid>
  );
}
