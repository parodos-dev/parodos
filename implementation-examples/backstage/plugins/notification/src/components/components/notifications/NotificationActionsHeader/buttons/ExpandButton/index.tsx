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
import MuiIconButton from '@material-ui/core/IconButton';

import { NotificationContext } from '../../../../../context/notifications';
import useMarkNotificationAsRead from '../../../../../hooks/useMarkNotificationAsRead';

import * as Styled from './styles';

const ExpandButton = () => {
  const markNotificationAsReadHook = useMarkNotificationAsRead();
  const notificationsContext = useContext(NotificationContext);

  return (
    <MuiIconButton
      disabled={!notificationsContext.allNotifications.length}
      onClick={() => {
        notificationsContext.setAllNotificationsExpanded(
          !notificationsContext.allNotificationsExpanded,
        );
        const unreadNotifications =
          notificationsContext.allNotifications.filter(
            notification => !notification.hasRead,
          );
        if (
          !notificationsContext.allNotificationsExpanded &&
          !!unreadNotifications.length
        ) {
          markNotificationAsReadHook.markNotificationAsRead({
            notificationIds: unreadNotifications.map(
              notification => notification.id,
            ),
          });
        }
      }}
    >
      {notificationsContext.allNotificationsExpanded ? (
        <Styled.CollapseIcon />
      ) : (
        <Styled.UnfoldIcon />
      )}
    </MuiIconButton>
  );
};

export default ExpandButton;
