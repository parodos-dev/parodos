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
import { useStyles } from './styles';
import { RunDetailContext } from '../../../../../../contexts/projects/runDetail';
import { Box, Button, Grid, Link, Typography } from '@material-ui/core';
import { post } from '../../../../../../utils/api';
import ToastContext from '../../../../../../contexts/toast';
import { CommitIcon } from '../../../../../common/icon/CommitIcon';

export default function CommitDetailCard() {
  const classes = useStyles();
  const runDetailContext = useContext(RunDetailContext);
  const { activeStageName, activeStageState } =
    runDetailContext.pipeline.pipelineState;
  const [clicked, setClicked] = useState(false);
  const toastContext = useContext(ToastContext);

  const isProDoneOrRunning =
    (sessionStorage.getItem('user_name') === 'lshannon'
      ? activeStageName === 'pro'
      : activeStageName === 'dev') ||
    activeStageState === 'completed' ||
    runDetailContext.pipeline.pipelineStages.some(
      stage => stage.state === 'active',
    );

  useEffect(() => {
    setClicked(isProDoneOrRunning);
  }, [isProDoneOrRunning]);

  const handleRunClick = async () => {
    try {
      setClicked(true);
      await post(`/api/v1/pipelines/run`, {
        name: runDetailContext.selectedBranch,
      });
    } catch (error) {
      toastContext.handleOpenToast(
        `Oops! Something went wrong. Please try again`,
      );
    }
  };

  return (
    <Grid container spacing={1} alignItems="center">
      <Grid
        item
        xs={10}
        style={{ display: 'flex', flexDirection: 'row', alignItems: 'center' }}
      >
        <Grid item>
          <CommitIcon fontSize={'large'} />
        </Grid>
        <Grid item pl={1}>
          <Box mr={2} className={classes.size14}>
            <Typography variant={'body1'}>
              {runDetailContext.pipeline.latestCommitDetail}
            </Typography>
          </Box>
        </Grid>
        <Grid item className={classes.size14}>
          <Link
            href={runDetailContext.pipeline.latestCommitUrl}
            target="_blanks"
            color="inherit"
            underline="always"
          >
            View in Github
          </Link>
        </Grid>
      </Grid>
      <Grid
        item
        style={{
          display: 'flex',
          justifyContent: 'flex-end',
          alignItems: 'center',
        }}
      >
        <Button
          disabled={isProDoneOrRunning || clicked}
          onClick={handleRunClick}
          variant={'contained'}
          color={'secondary'}
        >
          <Typography variant={'body2'}>run pipeline</Typography>
        </Button>
      </Grid>
    </Grid>
  );
}
