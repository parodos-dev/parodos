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
import './App.css';
import { ToastProvider } from './contexts/toast';
import { ThemeProvider } from '@material-ui/core';
import { ProjectsProvider } from './contexts/projects/projects';
import { parodosTheme } from './theme';
import { RunDetailProvider } from './contexts/projects/runDetail';
import Main from './pages/main';

export const ProviderWrappedApp = () => {
  return (
    <div>
      <ToastProvider>
        <ProjectsProvider>
          <RunDetailProvider>
            <ThemeProvider theme={parodosTheme}>
              <Main />
            </ThemeProvider>
          </RunDetailProvider>
        </ProjectsProvider>
      </ToastProvider>
    </div>
  );
};

export default ProviderWrappedApp;
