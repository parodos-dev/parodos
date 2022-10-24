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
import MuiFormControl from '@material-ui/core/FormControl';
import MuiMenuItem from '@material-ui/core/MenuItem';

import { NotificationContext } from '../../../context/notifications';

import * as NotificationTableSizeSelectorConfig from './config';
import * as Styled from './styles';

const NotificationTableSizeSelector = () => {
  const notificationsContext = useContext(NotificationContext);

  return (
    <Flex alignItems="center">
      <Styled.ShowText>SHOW</Styled.ShowText>
      <MuiFormControl>
        <Styled.WiderSelect
          onChange={event =>
            notificationsContext.setTableSize(event.target.value)
          }
          value={notificationsContext.tableSize}
        >
          {NotificationTableSizeSelectorConfig.tableSizes.map(tableSize => (
            <MuiMenuItem key={tableSize} value={tableSize}>
              {tableSize}
            </MuiMenuItem>
          ))}
        </Styled.WiderSelect>
      </MuiFormControl>
    </Flex>
  );
};

export default NotificationTableSizeSelector;
