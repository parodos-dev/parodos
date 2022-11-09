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
import { Flex } from 'rebass';

import SearchInput from '../../base/SearchInput';
import { NotificationContext } from '../../../context/notifications';
import MarkAsReadButton from './buttons/MarkAsReadButton';
import SelectAllCheckbox from './buttons/SelectAllCheckbox';
import ArchiveButton from './buttons/ArchiveButton';
import DeleteButton from './buttons/DeleteButton';
import ExpandButton from './buttons/ExpandButton';

import * as NotificationActionsHeaderUtils from './utils';
import * as Styled from './styles';

const NotificationActionsHeader = () => {
  const notificationsContext = useContext(NotificationContext);
  const atLeastOneNotificationIsSelected =
    !!notificationsContext.selectedNotifications.length;

  return (
    <Flex alignItems="center" justifyContent="space-between">
      <Flex alignItems="center">
        <SelectAllCheckbox />
        <MarkAsReadButton />
        <ArchiveButton />
        <DeleteButton />
        {atLeastOneNotificationIsSelected && (
          <Flex alignItems="center" ml="45px">
            <Styled.SelectedNotificationsText>
              {NotificationActionsHeaderUtils.getTextForSelectedNotifications(
                notificationsContext.selectedNotifications,
              )}
            </Styled.SelectedNotificationsText>
          </Flex>
        )}
      </Flex>
      <Flex alignItems="center">
        <SearchInput />
        <ExpandButton />
      </Flex>
    </Flex>
  );
};

export default NotificationActionsHeader;
