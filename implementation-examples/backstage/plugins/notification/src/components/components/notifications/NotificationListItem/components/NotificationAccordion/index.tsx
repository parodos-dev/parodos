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

import React, { useContext, useEffect, useState } from 'react';
import { Flex } from 'rebass';
import MuiAccordionSummary from '@material-ui/core/AccordionSummary';
import MuiAccordionDetails from '@material-ui/core/AccordionDetails';
import MuiExpandMoreIcon from '@material-ui/icons/ExpandMore';
import moment from 'moment';
import PropTypes from 'prop-types';

import { NotificationContext } from '../../../../../context/notifications';
import useMarkNotificationAsRead from '../../../../../hooks/useMarkNotificationAsRead';

import * as Styled from './styles';

const NotificationAccordion = ({ notification, notificationIsSelected }) => {
  const markNotificationAsReadHook = useMarkNotificationAsRead();
  const notificationsContext = useContext(NotificationContext);

  const [notificationIsExpandedState, setNotificationIsExpandedState] =
    useState(false);

  useEffect(() => {
    setNotificationIsExpandedState(
      notificationsContext.allNotificationsExpanded,
    );
  }, [notificationsContext.allNotificationsExpanded]);

  const expandNotification = expanded => {
    setNotificationIsExpandedState(expanded);
    if (!notification.hasRead && expanded) {
      markNotificationAsReadHook.markNotificationAsRead({
        notificationIds: [notification.id],
      });
    }
  };

  return (
    <Styled.Accordion
      selected={notificationIsSelected}
      onChange={(_, expanded) => expandNotification(expanded)}
      expanded={notificationIsExpandedState}
    >
      <MuiAccordionSummary expandIcon={<MuiExpandMoreIcon />}>
        <Styled.FullWidthFlexContainer alignItems="center" flex={2}>
          <Flex flex={1}>
            <Styled.NotificationTitle bold={!notification.hasRead}>
              {notification.subject}
            </Styled.NotificationTitle>
          </Flex>
          <Flex flex={1}>
            <Styled.NotificationText>
              {moment(notification.createdOn).format('LLLL')}
            </Styled.NotificationText>
          </Flex>
        </Styled.FullWidthFlexContainer>
      </MuiAccordionSummary>
      <MuiAccordionDetails>
        <Styled.AccordionDetailsContainer>
          <Styled.AccordionDetailsText>
            <div dangerouslySetInnerHTML={{ __html: notification.body }} />
          </Styled.AccordionDetailsText>
        </Styled.AccordionDetailsContainer>
      </MuiAccordionDetails>
    </Styled.Accordion>
  );
};

NotificationAccordion.propTypes = {
  notification: PropTypes.shape({}).isRequired,
  notificationIsSelected: PropTypes.bool.isRequired,
};

export default NotificationAccordion;
