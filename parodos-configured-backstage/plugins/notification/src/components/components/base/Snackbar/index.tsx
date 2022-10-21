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

import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import MuiSnackbar from '@material-ui/core/Snackbar';

import ToastContext from '../../../context/toast';

import * as Styled from './styles';

const Snackbar = ({ anchorOrigin, autoHideDuration }) => {
  const toastContext = useContext(ToastContext);

  return (
    <MuiSnackbar
      anchorOrigin={anchorOrigin}
      open={toastContext.toastIsOpen}
      autoHideDuration={autoHideDuration}
      onClose={toastContext.handleCloseToast}
    >
      <Styled.Alert
        onClose={toastContext.handleCloseToast}
        severity={toastContext.toastConfig.severity}
      >
        {toastContext.toastConfig.message}
      </Styled.Alert>
    </MuiSnackbar>
  );
};

Snackbar.propTypes = {
  anchorOrigin: PropTypes.shape({
    vertical: PropTypes.string,
    horizontal: PropTypes.string,
  }),
  autoHideDuration: PropTypes.number,
};

Snackbar.defaultProps = {
  anchorOrigin: {
    vertical: 'bottom',
    horizontal: 'center',
  },
  autoHideDuration: 4000,
};

export default Snackbar;
