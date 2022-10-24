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

import React from 'react';
import CssBaseline from '@material-ui/core/CssBaseline';

import AppBarTop from '../menu/AppBarTop';
import { Grid } from '@material-ui/core';

export default function Main({ children }) {
  return (
    <Grid container direction="column">
      <CssBaseline />
      <Grid item>
        <AppBarTop />
      </Grid>
      <Grid item xs>
        <main>{children}</main>
      </Grid>
    </Grid>
  );
}
