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
import MuiTabs from '@material-ui/core/Tabs';

import { NotificationContext } from '../../../context/notifications';

import * as Styled from './styles';

const Tabs = () => {
  const notificationsContext = useContext(NotificationContext);

  return (
    <React.Fragment>
      <MuiTabs
        variant="scrollable"
        TabIndicatorProps={{
          style: Styled.tabIndicatorStyles,
        }}
        value={notificationsContext.currentTab}
        onChange={(_, value) => {
          notificationsContext.setCurrentTab(value);
        }}
      >
        {Object.values(notificationsContext.allTabs).map(tab => (
          <Styled.Tab key={tab.label} label={tab.label} value={tab.label} />
        ))}
      </MuiTabs>
    </React.Fragment>
  );
};

export default Tabs;
