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
import Tabs from '../../components/notifications/Tabs';
import NotificationActionsHeader from '../../components/notifications/NotificationActionsHeader';
import NotificationList from '../../components/notifications/NotificationList';
import NotificationPaginationContainer from '../../components/notifications/NotificationPaginationContainer';
import Title from '../../components/base/Title';
import SectionContainer from '../../components/base/SectionContainer';
import axios from 'axios';

const Notifications = () => {
  axios.defaults.headers.common = {
    Authorization: `Bearer ${sessionStorage.getItem('access_token')}`,
  };
  return (
    <div style={{ padding: 50 }}>
      <div>
        <Title variant="h1" paragraph>
          Notifications
        </Title>
        <SectionContainer>
          <Tabs />
        </SectionContainer>
        <SectionContainer>
          <NotificationActionsHeader />
        </SectionContainer>
        <SectionContainer>
          <NotificationList />
        </SectionContainer>
        <NotificationPaginationContainer />
      </div>
    </div>
  );
};

export default Notifications;
