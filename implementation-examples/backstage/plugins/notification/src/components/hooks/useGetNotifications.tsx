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
import * as R from 'ramda';
import ToastContext from '../context/toast';
import { NotificationContext } from '../context/notifications';
import { useAxios } from '../config/axios';

const useGetNotifications = () => {
  const axios = useAxios();
  const notificationsContext = useContext(NotificationContext);
  const toastContext = useContext(ToastContext);
  const [isLoadingState, setIsLoadingState] = useState(false);

  const apiKeywordForTab = {
    ALL: '',
    UNREAD: 'UNREAD',
    ARCHIVED: 'ARCHIVED',
  };

  const getNotifications = async () => {
    try {
      setIsLoadingState(true);
      const getAllNotificationResponse = await axios.get(
        `/api/v1/notifications?state=${
          apiKeywordForTab[notificationsContext.currentTab]
        }`,
      );
      const notificationListFromResponse = R.pathOr(
        [],
        ['data', '_embedded', 'notificationrecords'],
        getAllNotificationResponse,
      );
      notificationsContext.setAllNotifications(notificationListFromResponse);
    } catch (error) {
      toastContext.handleOpenToast(
        `Oops! Something went wrong. Please try again`,
      );
    } finally {
      setIsLoadingState(false);
    }
  };

  return {
    getNotifications,
    isLoading: isLoadingState,
  };
};

export default useGetNotifications;
