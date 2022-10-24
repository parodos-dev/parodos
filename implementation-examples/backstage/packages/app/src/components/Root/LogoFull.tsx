/*
 * Copyright 2020 The Backstage Authors
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
 */

import React from 'react';
import { Box, makeStyles, Typography } from '@material-ui/core';
import IconButton from '@material-ui/core/IconButton';
import { useTheme } from '@material-ui/core/styles';

const useStyles = makeStyles({
  svg: {
    width: 'auto',
    height: 30,
  },
  path: {
    fill: '#7df3e1',
  },
});
const LogoFull = () => {
  const classes = useStyles();
  const theme = useTheme();
  return (
    <Box
      display="flex"
      alignItems="center"
      color={theme.palette.primary.contrastText}
    >
      <IconButton edge="start" className={classes.menuButton}>
        {/*<img src={LogoIcon} width="38" height="28" />*/}
      </IconButton>
      <Typography variant={'h5'} color={'textSecondary'}>
        Parodos
      </Typography>
    </Box>
  );
};

export default LogoFull;
