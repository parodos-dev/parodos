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
import { CssRadio } from './CssRadio';
import { Box, FormControlLabel, RadioGroup } from '@material-ui/core';
import { RunDetailContext } from '../../../../../../contexts/projects/runDetail';

const DateFilterSelection = () => {
  const runDetailContext = useContext(RunDetailContext);

  const handleChange = event => {
    runDetailContext.setFilter(event.target.value);
  };
  return (
    <Box>
      <Box mb={2} fontSize={12}>
        DISPLAY
      </Box>
      <RadioGroup
        aria-label="gender"
        name="gender1"
        value={runDetailContext.filter}
        onChange={handleChange}
      >
        <FormControlLabel value="all" control={<CssRadio />} label="All" />
        <FormControlLabel
          value="2weeks"
          control={<CssRadio />}
          label="Last 2 weeks"
        />
        <FormControlLabel
          value="error"
          control={<CssRadio />}
          label="Errors/Alerts"
        />
      </RadioGroup>
    </Box>
  );
};

export default DateFilterSelection;
