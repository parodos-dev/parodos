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

import React from 'react';
import { Flex } from 'rebass';

import NotificationPagination from '../NotificationPagination';
import NotificationTableSizeSelector from '../NotificationTableSizeSelector';

import * as Styled from './styles';

const NotificationPaginationContainer = () => {
  return (
    <React.Fragment>
      <Styled.DesktopContainer>
        <Flex flex={1} />
        <Flex flex={1} justifyContent="center">
          <NotificationPagination />
        </Flex>
        <Flex flex={1} justifyContent="flex-end">
          <NotificationTableSizeSelector />
        </Flex>
      </Styled.DesktopContainer>
      <Styled.MobileContainer>
        <Styled.FullWidthFlex flex={1} mb="20px" justifyContent="flex-end">
          <NotificationTableSizeSelector />
        </Styled.FullWidthFlex>
        <Flex flex={1} justifyContent="center">
          <NotificationPagination />
        </Flex>
      </Styled.MobileContainer>
    </React.Fragment>
  );
};

export default NotificationPaginationContainer;
