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
import { Box, Grid } from '@material-ui/core';
import { useStyles } from './styles';
import HealthDetailRow from '../HealthDetailRow';

export default function HealthDetailColumn({ ctg }) {
  const classes = useStyles();
  return (
    <Box>
      <Box mb={1}>{ctg.label}</Box>
      <Grid container>
        <Grid item>
          <Box className={classes.branch} />
        </Grid>
        <Grid item xs>
          {Object.keys(ctg.details).map((key, index) => (
            <HealthDetailRow key={index} {...{ detail: ctg.details[key] }} />
          ))}
        </Grid>
      </Grid>
    </Box>
  );
}
