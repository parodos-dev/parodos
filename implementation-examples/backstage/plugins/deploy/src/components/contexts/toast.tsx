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

import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

const ToastContext = createContext();

export const ToastProvider = ({ children }) => {
  const [toastIsOpenState, setToastIsOpenState] = useState(false);
  const [toastConfigState, setToastConfigState] = useState({});

  const handleCloseToast = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }

    setToastIsOpenState(false);
    setToastConfigState({});
  };

  const handleOpenToast = (message, severity = 'error') => {
    setToastConfigState({
      message,
      severity,
    });
    setToastIsOpenState(true);
  };

  return (
    <ToastContext.Provider
      value={{
        handleCloseToast,
        handleOpenToast,
        toastIsOpen: toastIsOpenState,
        setToastIsOpen: setToastIsOpenState,
        toastConfig: toastConfigState,
      }}
    >
      {children}
    </ToastContext.Provider>
  );
};

ToastProvider.propTypes = {
  children: PropTypes.element.isRequired,
};

export default ToastContext;
