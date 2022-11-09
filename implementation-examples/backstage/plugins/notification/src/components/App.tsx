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
import axios from 'axios';
import Notifications from './pages/notifications';
import NotificationProvider from './context/notifications';
import { ToastProvider } from './context/toast';
import Snackbar from './components/base/Snackbar';

const App = () => {
  const authBearerToken = localStorage.getItem('parodos-token');
  if (authBearerToken) {
    axios.defaults.headers.common = {
      Authorization: `Bearer ${authBearerToken}`,
    };
  }

  return (
    <div>
      <Snackbar />
      <Notifications />
    </div>
  );
};

export const ProviderWrappedApp = () => {
  return (
    <ToastProvider>
      <NotificationProvider>
        <App />
      </NotificationProvider>
    </ToastProvider>
  );
};

export default ProviderWrappedApp;
