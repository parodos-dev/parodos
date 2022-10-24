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
import { TextField } from '@material-ui/core';

export const TextInput = withStyles(theme => ({
  root: {
    width: '100%',
    '& .projects-MuiInput-underline': {
      '&:hover:not($disabled):not($focused):not($error):before': {
        borderBottom: `1px solid #2A2A2A`,
      },
    },
    '& .projects-MuiInputLabel-root': {
      color: '#A3ACB3',
    },
    '& .projects-MuiFormLabel-root.Mui-error': {
      color: '#DC536F !important',
    },
    '& label.projects-Mui-focused': {
      color: theme.palette.primary.main,
    },
    '& label.projects-MuiInputLabel-shrink': {
      color: theme.palette.primary.main,
    },
    '& .projects-MuiInput-underline:after': {
      borderWidth: 1,
      '&$error': {
        borderBottomColor: theme.palette.error,
      },
    },
    '& .projects-MuiInput-underline:before': {
      borderBottomColor: '#2A2A2A',
      borderWidth: 1,
    },
    '& input.projects-MuiInputBase-input': {
      color: '#2A2A2ABF',
      fontFamily: "'Roboto', sans-serif",
      fontSize: 14,
      padding: '7px 0 12px',
    },
    disabled: {},
    focused: {},
    error: {
      borderBottomColor: theme.palette.error,
      color: '#DC536F !important',
    },
  },
}))(TextField);
