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
import makeStyles from '@material-ui/core/styles/makeStyles';

export const useStyles = makeStyles(theme => ({
  root: {
    padding: '5px 11px',
    backgroundColor: theme.palette.primary.main,
    display: 'flex',
    width: 'fit-content',
    borderRadius: 2,
  },
  errRoot: {
    backgroundColor: theme.palette.error.main,
  },
  icon: {
    color: '#ffffff',
    fontSize: 20,
    alignSelf: 'center',
  },
  commitId: {
    color: theme.palette.secondary.contrastText,
    fontWeight: 'bold',
    fontSize: 16,
  },
  pre: {
    fontFamily: 'Roboto Mono',
    fontSize: 14,
    color: theme.palette.secondary.main,
  },
  preErr: {
    fontFamily: 'Roboto Mono',
    fontSize: 14,
    color: theme.palette.error.main,
  },
}));
