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
import { CssTabs } from './CssTabs';
import { CssTab } from './CssTab';
import { Box, Typography } from '@material-ui/core';
import ProcessesPanel from '../TabPanels/ProcessesPanel';
import TimelinePanel from '../TabPanels/TimelinePanel';
import HealthPanel from '../TabPanels/HealthPanel';
// import ProcessesPanel from '../panelCpnt/ProcessesPanel';
// import HealthPanel from '../panelCpnt/HealthPanel';
// import TimelinePanel from '../panelCpnt/TimelinePanel';
// import AccessPanel from '../panelCpnt/AccessPanel';

export default function RunDetailTabs() {
  const [value, setValue] = React.useState(0);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
      <div
        role="tabpanel"
        hidden={value !== index}
        id={`deploy-tabpanel-${index}`}
        aria-labelledby={`deploy-tab-${index}`}
        {...other}
      >
        {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
      </div>
    );
  }

  TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.number.isRequired,
    value: PropTypes.number.isRequired,
  };

  function a11yProps(index) {
    return {
      id: `deploy-tab-${index}`,
      'aria-controls': `deploy-tabpanel-${index}`,
    };
  }

  return (
    <Box sx={{ width: '100%' }}>
      <CssTabs value={value} onChange={handleChange} aria-label="tabs">
        <CssTab
          label={<Typography variant={'subtitle2'}>PROCESSES</Typography>}
          {...a11yProps(0)}
        />
        <CssTab
          label={<Typography variant={'subtitle2'}>HEALTH</Typography>}
          {...a11yProps(1)}
        />
        <CssTab
          label={<Typography variant={'subtitle2'}>TIMELINE</Typography>}
          {...a11yProps(2)}
        />
        <CssTab
          label={<Typography variant={'subtitle2'}>ACCESS</Typography>}
          {...a11yProps(3)}
        />
      </CssTabs>
      <TabPanel value={value} index={0}>
        <ProcessesPanel />
      </TabPanel>
      <TabPanel value={value} index={1}>
        <HealthPanel />
      </TabPanel>
      <TabPanel value={value} index={2}>
        <TimelinePanel />
        {/*<Typography>timeLine</Typography>*/}
      </TabPanel>
      <TabPanel value={value} index={3}>
        {/*<AccessPanel />*/}
        <Typography>access</Typography>
      </TabPanel>
    </Box>
  );
}
