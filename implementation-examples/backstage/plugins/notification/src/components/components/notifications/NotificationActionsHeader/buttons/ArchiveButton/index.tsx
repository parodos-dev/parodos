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

import useArchiveNotifications from '../../../../../hooks/useArchiveNotifications';
import { NotificationContext } from '../../../../../context/notifications';

import * as Styled from './styles';

const ArchiveButton = () => {
  const archiveNotificationsHook = useArchiveNotifications();
  const notificationsContext = useContext(NotificationContext);
  const atLeastOneNotificationIsSelected =
    !!notificationsContext.selectedNotifications.length;
  const buttonIsDisabled =
    !atLeastOneNotificationIsSelected ||
    archiveNotificationsHook.isLoading ||
    notificationsContext.currentTab ===
      notificationsContext.allTabs.ARCHIVED.label;

  return (
    <MuiTooltip title="Archive">
      <span>
        <MuiIconButton
          onClick={() =>
            archiveNotificationsHook.archiveNotifications(
              notificationsContext.selectedNotifications,
            )
          }
          disabled={buttonIsDisabled}
        >
          <Styled.ArchiveIcon disabled={buttonIsDisabled} />
        </MuiIconButton>
      </span>
    </MuiTooltip>
  );
};

export default ArchiveButton;
