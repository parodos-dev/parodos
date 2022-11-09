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
import {
  TableHead as MuiTableHead,
  TableRow,
  TableSortLabel,
} from '@material-ui/core';
import * as R from 'ramda';
import PropTypes from 'prop-types';

import * as TableHeadUtils from './utils';
import * as Styled from '../../styles';

const TableHead = ({ orderDateState, setOrderDateState }) => {
  return (
    <MuiTableHead>
      <TableRow>
        <Styled.NoBorderTableCell sortDirection={orderDateState}>
          <TableSortLabel
            active={!R.isEmpty(orderDateState)}
            direction={orderDateState}
            onClick={() =>
              TableHeadUtils.handleClickDateSorter({
                orderDateState,
                setOrderDateState,
              })
            }
          >
            <Styled.TableHeaderText>DATE</Styled.TableHeaderText>
          </TableSortLabel>
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          <Styled.TableHeaderText>
            ACADEMY RECOMMENDATION
          </Styled.TableHeaderText>
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          <Styled.TableHeaderText>TRIGGER</Styled.TableHeaderText>
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          <Styled.TableHeaderText>PROGRESS</Styled.TableHeaderText>
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          <Styled.TableHeaderText>CURRENT STATUS</Styled.TableHeaderText>
        </Styled.NoBorderTableCell>
      </TableRow>
    </MuiTableHead>
  );
};

TableHead.propTypes = {
  orderDateState: PropTypes.string.isRequired,
  setOrderDateState: PropTypes.func.isRequired,
};

export default TableHead;
