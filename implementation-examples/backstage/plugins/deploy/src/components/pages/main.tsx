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

import React, { useContext, useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import axios from 'axios';
import RunDetail from '../components/RunDetail';
import ToastContext from '../contexts/toast';
import Home from './index';
import { Box, Snackbar } from '@material-ui/core';
import Alert from '@material-ui/lab/Alert';
import { getUrl } from '../utils/getUrl';

function Wrapper() {
  const toastContext = useContext(ToastContext);

  return (
    <Box>
      <div style={{ margin: 15 }}>
        <Snackbar
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'center',
          }}
          open={toastContext.toastIsOpen}
          autoHideDuration={6000}
          onClose={toastContext.handleCloseToast}
        >
          <Alert
            onClose={() => toastContext.handleCloseToast}
            severity={toastContext.toastConfig.severity}
          >
            {toastContext.toastConfig.message}
          </Alert>
        </Snackbar>
        <Home />
      </div>
    </Box>
  );
}

export default function Main() {
  const url = getUrl();

  useEffect(() => {
    axios.defaults.headers.common = {
      Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
    };
    axios.defaults.baseURL = url;
  }, []);

  return (
    <Routes>
      <Route exact path="/" element={<Wrapper />} />
      <Route path="/runDetail/projectId/:projectId" element={<RunDetail />} />
    </Routes>
  );
}
