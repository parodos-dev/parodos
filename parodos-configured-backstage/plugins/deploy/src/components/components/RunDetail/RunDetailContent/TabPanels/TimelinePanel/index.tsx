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
import { useContext, useEffect, useState } from 'react';
import moment from 'moment';
import DateFilterSelection from './DateFilterSelection';
import TimelineStepper from './TimelineStepper';
import { Box, Button, Grid } from '@material-ui/core';
import { RunDetailContext } from '../../../../../contexts/projects/runDetail';

export default function TimelinePanel() {
  const runDetailContext = useContext(RunDetailContext);
  const [filterdTimeline, setFilterdTimeline] = useState([]);

  useEffect(() => {
    setFilterdTimeline(runDetailContext.events);
    if (runDetailContext.events) {
      const filterd = runDetailContext.events.filter(el => {
        switch (runDetailContext.filter) {
          case 'error':
            return el.status !== 'success';
          case '2weeks':
            return moment(el.date).isSameOrAfter(moment().subtract(14, 'days'));
          default:
            return true;
        }
      });
      setFilterdTimeline(filterd);
    }
  }, [runDetailContext.selectedProject, runDetailContext.filter]);
  return (
    <Box>
      <Box mt={4} mb={4}>
        {/*Lorem ipsum dolor sit amet consectetur adipiscing*/}
        {/*elit sed do eiusmod tempor*/}
      </Box>
      <Grid container>
        <Grid item xs={2}>
          <DateFilterSelection />
        </Grid>
        <Grid item xs={8}>
          <TimelineStepper timeline={filterdTimeline} />
        </Grid>
        <Grid>
          <Button
            variant={'outlined'}
            color={'secondary'}
            onClick={() => window.print()}
          >
            print
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
}
