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
import { makeStyles, withStyles } from '@material-ui/core/styles';
import { TimelineConnector } from '@material-ui/lab';

export const useStyles = makeStyles(theme => ({
  root: {
    padding: theme.spacing(1, 2),
    width: 'fit-content',
    minWidth: 300,
    position: 'relative',
    top: '-10%',
    backgroundColor: '#F6F8F2',
  },
  openIcon: {
    cursor: 'pointer',
  },
}));

export const CssTimelineConnector = withStyles(theme => ({
  root: {
    width: 1,
    borderLeft: '1px dashed #707070',
    backgroundColor: 'transparent',
  },
}))(TimelineConnector);
