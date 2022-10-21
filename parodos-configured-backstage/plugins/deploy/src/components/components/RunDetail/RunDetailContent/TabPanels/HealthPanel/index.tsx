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
import { Box, Grid } from '@material-ui/core';
import HealthDetailColumn from './HealthDetailColumn';
import { RunDetailContext } from '../../../../../contexts/projects/runDetail';

export default function HealthPanel() {
  const runDetailContext = useContext(RunDetailContext);
  const pipeline = runDetailContext.pipeline;
  return (
    <Box fontSize={14}>
      <Box mb={4} mt={3}>
        Most recent inspection of your application code quality.
      </Box>
      <Grid container spacing={2}>
        {pipeline?.health &&
          Object.keys(pipeline.health).map((ctg, index) => (
            <Grid item md={6} xs={6} lg={3} key={index}>
              <HealthDetailColumn {...{ ctg: pipeline.health[ctg] }} />
            </Grid>
          ))}
      </Grid>
    </Box>
  );
}
