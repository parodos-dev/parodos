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
import PropTypes from 'prop-types';
import Autocomplete from '@material-ui/lab/Autocomplete';

import * as Styled from './styles';

const AutoCompleteInput = ({
  onChange,
  autoCompleteData,
  disabled,
  placeholder,
  getOptionLabel,
}) => {
  return (
    <Autocomplete
      disabled={disabled}
      getOptionLabel={getOptionLabel}
      // value={value.name}
      onChange={(_, data) => {
        onChange(data);
      }}
      options={autoCompleteData}
      renderInput={params => (
        <Styled.FixedWidthTextField
          {...params}
          placeholder={placeholder}
          margin="normal"
        />
      )}
    />
  );
};

AutoCompleteInput.propTypes = {
  value: PropTypes.object,
  onChange: PropTypes.func.isRequired,
  autoCompleteData: PropTypes.array,
  disabled: PropTypes.bool,
  placeholder: PropTypes.string,
  getOptionLabel: PropTypes.func,
};

AutoCompleteInput.defaultProps = {
  autoCompleteData: [],
  disabled: false,
  placeholder: 'Employee Name or ID',
  getOptionLabel: option => option,
};

export default AutoCompleteInput;
