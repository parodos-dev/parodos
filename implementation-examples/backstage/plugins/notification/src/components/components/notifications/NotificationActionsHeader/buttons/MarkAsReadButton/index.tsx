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
import MuiTooltip from '@material-ui/core/Tooltip';

import useMarkNotificationAsRead from '../../../../../hooks/useMarkNotificationAsRead';
import { NotificationContext } from '../../../../../context/notifications';
import ToastContext from '../../../../../context/toast';
import MarkAsReadIcon from '../../../../../assets/icons/MarkAsReadIcon';

const MarkAsReadButton = () => {
  const toastContext = useContext(ToastContext);
  const notificationsContext = useContext(NotificationContext);
  const markNotificationAsReadHook = useMarkNotificationAsRead();

  const atLeastOneMessageIsUnread =
    notificationsContext.selectedNotifications.some(notificationId => {
      const notificationFromContext =
        notificationsContext.allNotifications.find(
          notification => notification.id === notificationId,
        ) || {};
      return !notificationFromContext.hasRead;
    });

  const buttonIsDisabled =
    !notificationsContext.selectedNotifications.length ||
    markNotificationAsReadHook.isLoading ||
    !atLeastOneMessageIsUnread;

  return (
    <MuiTooltip title="Mark as read">
      <span>
        <MuiIconButton
          onClick={() =>
            markNotificationAsReadHook.markNotificationAsRead({
              notificationIds: notificationsContext.selectedNotifications,
              onSuccess: notificationIds => {
                notificationsContext.setSelectedNotifications([]);
                toastContext.handleOpenToast(
                  `Notification${
                    notificationIds.length > 1 ? 's' : ''
                  } marked as read`,
                  'success',
                );
              },
            })
          }
          disabled={buttonIsDisabled}
        >
          <MarkAsReadIcon disabled={buttonIsDisabled} />
        </MuiIconButton>
      </span>
    </MuiTooltip>
  );
};

export default MarkAsReadButton;
