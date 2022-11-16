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
import PropTypes from 'prop-types';
import MuiCheckbox from '@material-ui/core/Checkbox';

import GreenCheckIcon from '../../base/GreenCheckIcon';
import { NotificationContext } from '../../../context/notifications';
import NotificationAccordion from './components/NotificationAccordion';

import * as NotificationListItemUtils from './utils';
import * as Styled from './styles';

const NotificationListItem = ({ notification }) => {
  const notificationsContext = useContext(NotificationContext);

  const notificationIsSelected =
    notificationsContext.selectedNotifications.includes(notification.id);

  return (
    <Styled.Container alignItems="center">
      <MuiCheckbox
        checkedIcon={<GreenCheckIcon />}
        checked={notificationIsSelected}
        onClick={() =>
          NotificationListItemUtils.handleSelectNotification({
            notificationIsSelected,
            notificationsContext,
            notification,
          })
        }
      />
      <Flex flex={1}>
        <NotificationAccordion
          notification={notification}
          notificationIsSelected={notificationIsSelected}
        />
      </Flex>
    </Styled.Container>
  );
};

NotificationListItem.propTypes = {
  notification: PropTypes.shape({}).isRequired,
};

export default NotificationListItem;
