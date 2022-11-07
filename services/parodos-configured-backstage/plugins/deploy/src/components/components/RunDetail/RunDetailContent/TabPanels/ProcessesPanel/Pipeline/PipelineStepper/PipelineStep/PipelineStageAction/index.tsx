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

import React, { useContext, useState } from 'react';
import { Box, Button, Grid, Link, Typography } from '@material-ui/core';
import { RunDetailContext } from '../../../../../../../../../contexts/projects/runDetail';
import makeStyles from '@material-ui/core/styles/makeStyles';

const useStyles = makeStyles(theme => ({
  root: {},
}));

export default function PipelineStageAction({
  state,
  runDetailUrl,
  stageName,
}) {
  const classes = useStyles();
  const runDetailContext = useContext(RunDetailContext);
  const { activeStageName, activeStageState } =
    runDetailContext.pipeline.pipelineState;
  const isProDone =
    activeStageName === 'pro' || activeStageState === 'completed';
  const [envUpdated, setEnvUpdated] = useState(false);
  const [reRunClicked, setReRunClicked] = useState(false);
  const handleErrClick = () => {
    window.open(runDetailUrl);
  };
  const handleToggleDialog = (error = false) => {
    if (!error) {
      runDetailContext.setOpen(!runDetailContext.open);
      console.log('vars');
    } else {
      runDetailContext.setOpenErrorLog(!runDetailContext.openErrorLog);
    }
    runDetailContext.setStage(stageName);
  };
  const handleUpdated = () => {
    setEnvUpdated(true);
  };
  const handleClickRerun = () => {
    setReRunClicked(true);
  };
  return (
    state !== 'inactive' && (
      <Box display={'flex'} style={{ flexDirection: 'column' }}>
        {!isProDone && (
          <Box mb={2}>
            <Button
              variant={'outlined'}
              onClick={() => handleToggleDialog()}
              color={'secondary'}
            >
              Configure env
            </Button>
          </Box>
        )}
        <Grid container height={3} mb={2}>
          {state !== 'active' && (
            <Button
              variant={'outlined'}
              onClick={handleClickRerun}
              disabled={state !== 'error' && !envUpdated}
              color={'secondary'}
            >
              Re-run {stageName}
            </Button>
          )}
        </Grid>
        <Grid container height={3} mt={1}>
          <Box mt={2}>
            {reRunClicked && (
              <Box mb={1}>
                <Link>View application</Link>
              </Box>
            )}
            {state === 'error' ? (
              <Button onClick={() => handleToggleDialog(true)}>
                <Typography variant={'body1'}>View error log</Typography>
              </Button>
            ) : (
              <Link href="#" color="inherit">
                <Typography variant={'body1'} color={'secondary'}>
                  Run details
                </Typography>
              </Link>
            )}
          </Box>
        </Grid>
      </Box>
    )
  );
}
