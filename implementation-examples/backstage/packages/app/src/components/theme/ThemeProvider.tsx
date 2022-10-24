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

import {
  createTheme,
  genPageTheme,
  lightTheme,
  shapes,
} from '@backstage/theme';

export const outerTheme = createTheme({
  defaultPageTheme: 'home',
  palette: {
    ...lightTheme.palette,
    primary: {
      light: '#434343',
      main: '#002D72',
      dark: '#2A2A2A',
      contrastText: '#FFFFFF',
    },
    secondary: {
      light: '#C0D554',
      main: '#C0D554',
      dark: '#C0D554',
      contrastText: '##C0D554',
    },
    error: {
      main: '#DC536F',
    },
    text: {
      secondary: '#FFFFFF',
    },
  },
  fontFamily: "'Roboto', sans-serif",
  /* below drives the header colors */
  pageTheme: {
    home: genPageTheme(['#8c4351', '#343b58'], shapes.wave),
  },
});
