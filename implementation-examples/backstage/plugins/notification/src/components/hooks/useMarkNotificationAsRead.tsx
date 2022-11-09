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

import { useContext, useState } from 'react';
import { useAxios } from '../config/axios';
import ToastContext from '../context/toast';
import { NotificationContext } from '../context/notifications';

const useMarkNotificationAsRead = () => {
  const axios = useAxios();
  const notificationsContext = useContext(NotificationContext);
  const toastContext = useContext(ToastContext);
  const [isLoadingState, setIsLoadingState] = useState(false);

  const markNotificationAsRead = async ({
    notificationIds,
    onSuccess = () => {},
  }) => {
    try {
      setIsLoadingState(true);
      const notificationsToMarkAsRead =
        notificationsContext.allNotifications.filter(notification => {
          return (
            notificationIds.includes(notification.id) && !notification.hasRead
          );
        });
      await Promise.all(
        notificationIds.map(async notificationId => {
          await axios.put(
            `/api/v1/notifications/update/hasread/${notificationId}`,
          );
        }),
      );
      const newListOfNotifications = notificationsContext.allNotifications.map(
        notification => {
          if (notificationIds.includes(notification.id)) {
            return {
              ...notification,
              hasRead: true,
            };
          }
          return notification;
        },
      );
      notificationsContext.setAllNotifications(newListOfNotifications);
      onSuccess(notificationsToMarkAsRead.length);
    } catch (error) {
      toastContext.handleOpenToast(
        `Oops! Something went wrong. Please try again`,
      );
    } finally {
      setIsLoadingState(false);
    }
  };

  return {
    markNotificationAsRead,
    isLoading: isLoadingState,
  };
};

export default useMarkNotificationAsRead;
