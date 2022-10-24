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

import React, { useContext, useEffect } from 'react';
import Snackbar from '@material-ui/core/Snackbar';
import MuiAlert from '@material-ui/lab/Alert';
import Migration from './pages/migration';
import ToastContext, { ToastProvider } from './context/toast';
import { Box, ThemeProvider } from '@material-ui/core';
import { parodosTheme } from './theme';
import axios from 'axios';
import { getUrl } from './util/getUrl';

function App() {
  const toastContext = useContext(ToastContext);
  const url = getUrl();

  useEffect(() => {
    axios.defaults.headers.common = {
      Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
    };
    axios.defaults.baseURL = url;
  }, []);

  return (
    <div style={{ padding: 10 }}>
      <Snackbar
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center',
        }}
        open={toastContext.toastIsOpen}
        autoHideDuration={6000}
        onClose={toastContext.handleCloseToast}
      >
        <MuiAlert
          onClose={toastContext.handleCloseToast}
          severity={toastContext.toastConfig.severity}
        >
          {toastContext.toastConfig.message}
        </MuiAlert>
      </Snackbar>
      <Migration />
    </div>
  );
}

export const ProviderWrappedApp = () => {
  return (
    <Box>
      <ToastProvider>
        <ThemeProvider theme={parodosTheme}>
          <App />
        </ThemeProvider>
      </ToastProvider>
    </Box>
  );
};

export default ProviderWrappedApp;
