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

import { useContext } from 'react';
import { NotificationContext } from '../../../../context/notifications';
import useUpdateOnlyEffect from '../../../../hooks/useUpdateOnlyEffect';
import useSearchNotifications from '../../../../hooks/useSearchNotifications';
import useGetNotifications from '../../../../hooks/useGetNotifications';
import useDebounce from '../../../../hooks/useDebounce';

const useSearchInput = () => {
  const searchNotificationsHook = useSearchNotifications();
  const getNotificationsHook = useGetNotifications();
  const notificationsContext = useContext(NotificationContext);
  const debouncedSearchTermValue = useDebounce({
    value: notificationsContext.searchText,
    delay: 1000,
  });

  useUpdateOnlyEffect(() => {
    if (notificationsContext.searchText) {
      searchNotificationsHook.searchNotifications(
        notificationsContext.searchText,
      );
    } else {
      getNotificationsHook.getNotifications();
    }
  }, [debouncedSearchTermValue]);

  return {};
};

export default useSearchInput;
