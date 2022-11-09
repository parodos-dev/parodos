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
import { Box, Grid, Typography } from '@material-ui/core';
import OpenInNewIcon from '@material-ui/icons/OpenInNew';
import clsx from 'clsx';
import { useStyles } from './styles';
import ArrowUpwardIcon from '@material-ui/icons/ArrowUpward';
import ArrowDownwardIcon from '@material-ui/icons/ArrowDownward';

interface IHealthDetailRow {
  detail;
}
const HealthDetailRow = ({ detail }: IHealthDetailRow) => {
  const classes = useStyles();
  const { transition, number, label, status } = detail;
  const isPercentage = ['Coverage %'].includes(label);
  const isDanger = status.statusLabel === 'danger';

  const handleExternalLink = () => {
    window.open(status.url);
  };

  return (
    <Grid item alignItems="center" xs container className={classes.rowRoot}>
      <Grid item xs={7} zeroMinWidth>
        <Typography
          noWrap
          className={clsx(classes.rowText, isDanger && classes.dangerLabel)}
        >
          {label}
          {isDanger && (
            <OpenInNewIcon
              onClick={handleExternalLink}
              className={classes.externalLink}
            />
          )}
        </Typography>
      </Grid>
      <Grid item xs={5}>
        <Box width="100%" className={isDanger ? classes.dangerNumber : ''}>
          <Box component="span" mr={1}>
            {number}
            {isPercentage && '%'}
          </Box>
          <Box component="span" display="inline-flex" alignItems="center">
            (
            {transition.startsWith('-') ? (
              <>
                <ArrowDownwardIcon
                  className={classes.arrow}
                  color={'primary'}
                />
                {transition.substring(1)}
                {isPercentage && '%'}
              </>
            ) : (
              <>
                <ArrowUpwardIcon
                  className={classes.arrow}
                  color={'primary.dark'}
                />
                {transition}
                {isPercentage && '%'}
              </>
            )}
            )
          </Box>
        </Box>
      </Grid>
    </Grid>
  );
};

export default HealthDetailRow;
