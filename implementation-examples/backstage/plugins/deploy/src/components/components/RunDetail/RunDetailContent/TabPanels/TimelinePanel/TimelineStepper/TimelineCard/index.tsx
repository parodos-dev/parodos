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
import OpenInNewIcon from '@material-ui/icons/OpenInNew';
import { useStyles } from './styles';
import { Box, Grid, Paper } from '@material-ui/core';
import HighIcon from './HightIcon';
import moment from 'moment-timezone';

export default function TimelineCard({
  label,
  url,
  date,
  status,
  gitMsg,
  user,
}) {
  const classes = useStyles();
  const handleClickExternal = url => {
    window.open(url);
  };

  const timeToString = dateTime => {
    return moment(dateTime).tz('EST').format('ddd MMM D hh:mm A z');
  };

  return (
    <Paper className={classes.root} elevation={0}>
      <Grid container alignItems="center">
        <Grid item xs>
          <Box mr={4}>
            <Box fontSize={14} display="flex">
              {label}
              {status === 'alert' && (
                <Box component="span" ml={2} display="flex" alignItems="center">
                  <HighIcon />
                </Box>
              )}
            </Box>
            <Box fontSize={11}>
              {gitMsg && gitMsg}
              {user && <> by {user}</>}
              {(user || gitMsg) && <> on </>}
              {timeToString(date)}
            </Box>
          </Box>
        </Grid>
        {url && status !== 'success' && (
          <Grid item>
            <Box display="flex">
              <OpenInNewIcon
                className={classes.openIcon}
                onClick={() => handleClickExternal(url)}
              />
            </Box>
          </Grid>
        )}
      </Grid>
    </Paper>
  );
}
