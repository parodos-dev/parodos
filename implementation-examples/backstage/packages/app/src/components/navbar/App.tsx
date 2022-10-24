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

import makeStyles from '@material-ui/core/styles/makeStyles';
import React from 'react';
import {
  createGenerateClassName,
  StylesProvider,
} from '@material-ui/core/styles';
import AppbarTop from './component/menu/AppBarTop';

const generateClassName = createGenerateClassName({
  productionPrefix: 'Parodos',
  seed: 'parodos',
});

const useGlobalStyles = makeStyles({
  '@global': {
    body: {
      margin: 0,
    },
  },
});

const App = ({ logout }: any) => {
  useGlobalStyles();
  return (
    <StylesProvider generateClassName={generateClassName}>
      <AppbarTop {...{ logout }} />
    </StylesProvider>
  );
};

export default App;
