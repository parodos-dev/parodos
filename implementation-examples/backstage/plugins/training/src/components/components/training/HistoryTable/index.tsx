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
import { Table, TableBody } from '@material-ui/core';

import { TrainingContext } from '../../../context/training';
import TableHead from './components/TableHead';
import TableBodyRow from './components/TableBodyRow';

import * as HistoryTableUtils from './utils';
import * as Styled from './styles';

const HistoryTable = () => {
  const [orderDateState, setOrderDateState] = useState('asc');
  const [sortDataFunction, setSortDataFunction] = useState(undefined);
  const trainingContext = useContext(TrainingContext);

  useEffect(() => {
    if (orderDateState) {
      HistoryTableUtils.handleSortDataByDate({
        orderDateState,
        setSortDataFunction,
      });
    } else {
      setSortDataFunction(undefined);
    }
  }, [orderDateState]);

  const sortedData = trainingContext.employeeHistory.sort(sortDataFunction);

  return (
    <Styled.Container>
      <Styled.Title>History</Styled.Title>
      <Table>
        <TableHead
          orderDateState={orderDateState}
          setOrderDateState={setOrderDateState}
        />
        <TableBody>
          {sortedData.map(employeeHistoryItem => (
            <TableBodyRow
              employeeHistoryItem={employeeHistoryItem}
              key={employeeHistoryItem.id}
            />
          ))}
        </TableBody>
      </Table>
      {!trainingContext.employeeHistory.length && (
        <Styled.TableEmptyStateRow>
          <Styled.EmptyStateText>No data to display</Styled.EmptyStateText>
        </Styled.TableEmptyStateRow>
      )}
    </Styled.Container>
  );
};

export default HistoryTable;
