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
import MuiIconButton from '@material-ui/core/IconButton';

import { NotificationContext } from '../../../context/notifications';

import * as SearchInputHooks from './hooks';
import * as Styled from './styles';

const SearchInput = () => {
  SearchInputHooks.useSearchInput();
  const notificationsContext = useContext(NotificationContext);

  return (
    <Flex alignItems="center">
      <MuiIconButton
        onClick={() =>
          notificationsContext.setSearchInputOpen(
            !notificationsContext.searchInputOpen,
          )
        }
      >
        <Styled.SearchIcon />
      </MuiIconButton>
      {notificationsContext.searchInputOpen && (
        <Styled.DynamicSearchField
          inputProps={{
            maxLength: 15,
          }}
          value={notificationsContext.searchText}
          onChange={event =>
            notificationsContext.setSearchText(event.target.value)
          }
          placeholder="Search"
          type="search"
        />
      )}
    </Flex>
  );
};

export default SearchInput;
