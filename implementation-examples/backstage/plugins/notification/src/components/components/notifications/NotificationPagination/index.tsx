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
import MuiChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import MuiChevronRightIcon from '@material-ui/icons/ChevronRight';
import MuiIconButton from '@material-ui/core/IconButton';
import { NotificationContext } from '../../../context/notifications';

import * as Styled from './styles';

const NotificationPagination = () => {
  const notificationsContext = useContext(NotificationContext);
  // TO-DO: get this value from the context eventually
  const totalPages = 1;

  const disableFirstPageButton = notificationsContext.currentPage === 1;
  const disablePreviousPageButton = notificationsContext.currentPage - 1 < 1;
  const disableNextPageButton =
    notificationsContext.currentPage + 1 > totalPages;
  const disableLastPageButton = notificationsContext.currentPage === totalPages;

  return (
    <Flex alignItems="center">
      <Flex mr="10px">
        <Styled.FirstAndLastButton disabled={disableFirstPageButton}>
          FIRST
        </Styled.FirstAndLastButton>
      </Flex>
      <MuiIconButton disabled={disablePreviousPageButton} edge="start">
        <MuiChevronLeftIcon />
      </MuiIconButton>
      <Styled.PageText>{`${notificationsContext.currentPage} of ${totalPages}`}</Styled.PageText>
      <MuiIconButton disabled={disableNextPageButton} edge="end">
        <MuiChevronRightIcon />
      </MuiIconButton>
      <Flex ml="10px">
        <Styled.FirstAndLastButton disabled={disableLastPageButton}>
          LAST
        </Styled.FirstAndLastButton>
      </Flex>
    </Flex>
  );
};

export default NotificationPagination;
