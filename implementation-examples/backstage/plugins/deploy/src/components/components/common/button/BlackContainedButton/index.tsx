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

import { withStyles } from '@material-ui/core/styles';
import { Button } from '@material-ui/core';

export const BlackContainedButton = withStyles(theme => ({
  root: {
    border: '2px solid',
    lineHeight: 1,
    color: theme.palette.primary.contrastText,
    backgroundColor: theme.palette.primary.main,
    borderColor: theme.palette.primary.main,
    fontWeight: 400,
    textTransform: 'none',
    padding: theme.spacing(2, 3),
    '&:hover': {
      backgroundColor: '#434343',
    },
    '&:active': {
      backgroundColor: '#434343',
    },
    '&:focus': {
      backgroundColor: '#434343',
    },
    '&$disabled': {
      color: theme.palette.primary.contrastText,
      backgroundColor: theme.palette.primary.main,
      opacity: 0.8,
    },
  },
  disabled: {},
}))(Button);
