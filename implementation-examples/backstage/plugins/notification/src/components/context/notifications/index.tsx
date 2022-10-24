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

import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

import { NOTIFICATION_TABS } from '../../constants/notifications';
import useUpdateOnlyEffect from '../../hooks/useUpdateOnlyEffect';

export const NotificationContext = createContext();

const NotificationProvider = ({ children }) => {
  const [allNotificationsState, setAllNotificationsState] = useState([]);
  const [currentTabState, setCurrentTabState] = useState(
    NOTIFICATION_TABS.ALL.label,
  );
  const [allNotificationsExpandedState, setAllNotificationsExpandedState] =
    useState(false);
  const [selectedNotificationsState, setSelectedNotificationsState] = useState(
    [],
  );
  const [tableSizeState, setTableSizeState] = useState(10);
  const [currentPageState, setCurrentPageState] = useState(1);
  const [searchTextState, setSearchTextState] = useState('');
  const [searchInputOpenState, setSearchInputOpenState] = useState(false);

  useUpdateOnlyEffect(() => {
    setAllNotificationsExpandedState(false);
    setSelectedNotificationsState([]);
    setCurrentPageState(1);
    setSearchTextState('');
    setSearchInputOpenState(false);
  }, [currentTabState]);

  return (
    <NotificationContext.Provider
      value={{
        allTabs: NOTIFICATION_TABS,
        currentTab: currentTabState,
        setCurrentTab: setCurrentTabState,
        allNotifications: allNotificationsState,
        setAllNotifications: setAllNotificationsState,
        allNotificationsExpanded: allNotificationsExpandedState,
        setAllNotificationsExpanded: setAllNotificationsExpandedState,
        selectedNotifications: selectedNotificationsState,
        setSelectedNotifications: setSelectedNotificationsState,
        tableSize: tableSizeState,
        setTableSize: setTableSizeState,
        currentPage: currentPageState,
        setCurrentPage: setCurrentPageState,
        searchText: searchTextState,
        setSearchText: setSearchTextState,
        searchInputOpen: searchInputOpenState,
        setSearchInputOpen: setSearchInputOpenState,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

NotificationProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export default NotificationProvider;
