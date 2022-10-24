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

import React from 'react';
import CheckIcon from '@material-ui/icons/Check';
import clsx from 'clsx';
import { useStyles } from './styles';
import { Box } from '@material-ui/core';
import './styles/loaderdots.css';

const getSubStageStage = (classes, state) => {
  switch (state) {
    case 'completed':
      return <CheckIcon className={classes.checkIcon} />;
    case 'active':
      return <div className="dot-loader" />;
    default:
      return <></>;
  }
};
export default function PipelineSubStages({ subStages, state }) {
  const classes = useStyles();

  return (
    <Box className={classes.root}>
      {subStages.map((stage, index) => {
        const isGrey = state !== 'inactive' && stage.state === 'inactive';
        return (
          <Box
            key={index}
            display="flex"
            alignItems="center"
            className={clsx(
              isGrey && classes.inactive,
              stage.state === 'error' && classes.error,
            )}
            mb={1}
          >
            {stage.stageName}
            {(state === 'active' || state === 'error') &&
              getSubStageStage(classes, stage.state)}
          </Box>
        );
      })}
    </Box>
  );
}
