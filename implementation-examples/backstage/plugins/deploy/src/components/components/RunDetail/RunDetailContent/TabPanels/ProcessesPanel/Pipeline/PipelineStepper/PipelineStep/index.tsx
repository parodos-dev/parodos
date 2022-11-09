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

import HighlightOffIcon from '@material-ui/icons/HighlightOff';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import ScheduleIcon from '@material-ui/icons/Schedule';
// import PipelineSubStages from "./PipelineSubStages";
// import PipelineStageAction from "./PipelinStageAction";
import makeStyles from '@material-ui/core/styles/makeStyles';
import { Box, Grid } from '@material-ui/core';
import PipelineSubStages from './PipelineSubStages';
import clsx from 'clsx';
import PipelineStageAction from './PipelineStageAction';

const useStyles = makeStyles(theme => ({
  inactive: {
    opacity: 0.4,
  },
  hr: {
    width: '90%',
    border: 'none',
    height: 1,
    borderTop: '1px dashed #707070',
  },
  checkCircleIcon: {
    color: theme.palette.primary.main,
  },
  errorIcon: {
    color: theme.palette.error.main,
  },
  stageName: {
    textTransform: 'uppercase',
  },
}));
const getStageStateIcon = (classes, state) => {
  switch (state) {
    case 'error':
      return <HighlightOffIcon className={classes.errorIcon} />;
    case 'completed':
      return <CheckCircleIcon className={classes.checkCircleIcon} />;
    default:
      return <ScheduleIcon />;
  }
};
export default function PipelineStep({ stage, isLast }) {
  const classes = useStyles();
  const { subStages, state, runDetailUrl } = stage;
  return (
    <Grid
      item
      container
      xs
      alignItems="center"
      className={clsx(state === 'inactive' && classes.inactive)}
    >
      <Grid item>
        <Box display="flex" alignItems="center">
          {getStageStateIcon(classes, state)}
          <Box position="relative" ml={1}>
            <span className={classes.stageName}>{stage.stageName}</span>
            <Box position="absolute" width="max-content" mt={2}>
              <PipelineSubStages {...{ subStages, state }} />
              <PipelineStageAction
                {...{ state, runDetailUrl, stageName: stage.stageName }}
              />
            </Box>
          </Box>
        </Box>
      </Grid>
      {!isLast && (
        <Grid item xs>
          <hr className={classes.hr} />
        </Grid>
      )}
    </Grid>
  );
}
