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
import MuiCircularProgress from '@material-ui/core/CircularProgress';

import { NotificationContext } from '../../../context/notifications';
import useGetNotifications from '../../../hooks/useGetNotifications';

import * as NotificationListHooks from './hooks';
import * as NotificationListUtils from './utils';
import * as Styled from './styles';

const NotificationList = () => {
  NotificationListHooks.useNotificationList();
  const getNotificationsHook = useGetNotifications();
  const notificationsContext = useContext(NotificationContext);

  return (
    <Styled.Container>
      {getNotificationsHook.isLoading ? (
        <Styled.ContentContainer>
          <MuiCircularProgress />
        </Styled.ContentContainer>
      ) : (
        NotificationListUtils.renderNotificationListContent(
          notificationsContext.allNotifications,
        )
      )}
    </Styled.Container>
  );
};

export default NotificationList;
