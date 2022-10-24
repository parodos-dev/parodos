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
 * @author Luke Shannon (Github: lshannon)
 */

import { createTheme } from '@material-ui/core';

export const parodosTheme = createTheme({
  palette: {
    secondary: {
      light: '#434343',
      main: '#2A2A2A',
      dark: '#2A2A2A',
      contrastText: '#fff',
    },
    primary: {
      light: '#C0D554',
      main: '#C0D554',
      dark: '#C0D700',
      contrastText: '#000',
    },
    error: {
      main: '#DC536F',
    },
    text: {
      primary: '#0C1D31',
      secondary: '#2A2A2A',
    },
    background: {
      paper: '#F6F8F2',
      default: '#fff',
    },
  },
  typography: {
    fontFamily: "'Roboto', sans-serif",
    title: {
      fontSize: 30,
      fontFamily: "'Roboto', sans-serif",
      fontWeight: 500,
    },
    h5: {
      fontSize: 18,
      fontFamily: "'Roboto', sans-serif",
      fontWeight: 500,
    },
    body1: {
      fontSize: 14,
      fontFamily: "'Roboto', sans-serif",
    },
    subtitle1: {
      fontFamily: "'Roboto', sans-serif",
      fontSize: 24,

      fontWeight: 500,
    },
    subtitle2: {
      fontFamily: "'Roboto', sans-serif",
      fontSize: 14,

      fontWeight: 500,
    },
  },
  components: {
    // Name of the component
    MuiListItemButton: {
      styleOverrides: {
        root: {
          '&.Mui-selected': {
            backgroundColor: '#F3F8D0',
            '&:hover': { background: '#F3F8D0' },
          },
        },
      },
    },
  },
});
