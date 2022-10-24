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

import TableRow from './components/TableRow';

import * as RecentActivityConfig from './config';
import * as Styled from './styles';

const RecentActivity = () => {
  const { recentActivityConfig } = RecentActivityConfig;

  return (
    <Styled.Container>
      <Styled.Title>Recent Activity</Styled.Title>
      <Flex flex={3}>
        <Flex flex={1}>
          <Styled.FullWidthTable>
            <tbody>
              <TableRow
                label={recentActivityConfig.eventName.label}
                dataKey={recentActivityConfig.eventName.dataKey}
              />
              <TableRow
                label={recentActivityConfig.dateOfAssignment.label}
                dataKey={recentActivityConfig.dateOfAssignment.dataKey}
                formatFunction={
                  recentActivityConfig.dateOfAssignment.formatFunction
                }
              />
              <TableRow
                label={recentActivityConfig.assignmentTrigger.label}
                dataKey={recentActivityConfig.assignmentTrigger.dataKey}
              />
            </tbody>
          </Styled.FullWidthTable>
        </Flex>
        <Flex flex={1} pr="70px" pl="70px">
          <Styled.FullWidthTable>
            <tbody>
              <TableRow
                label={recentActivityConfig.initialEventPoint.label}
                dataKey={recentActivityConfig.initialEventPoint.dataKey}
                formatFunction={
                  recentActivityConfig.initialEventPoint.formatFunction
                }
              />
              <TableRow
                label={recentActivityConfig.academyRecommendation.label}
                dataKey={recentActivityConfig.academyRecommendation.dataKey}
                formatFunction={
                  recentActivityConfig.academyRecommendation.formatFunction
                }
              />
              <TableRow label="PROGRESS" dataKey="progress" />
            </tbody>
          </Styled.FullWidthTable>
        </Flex>
        <Flex flex={1}>
          <Styled.FullWidthTable>
            <tbody>
              <TableRow
                label={recentActivityConfig.currentStatus.label}
                dataKey={recentActivityConfig.currentStatus.dataKey}
                formatFunction={
                  recentActivityConfig.currentStatus.formatFunction
                }
              />
            </tbody>
          </Styled.FullWidthTable>
        </Flex>
      </Flex>
    </Styled.Container>
  );
};

export default RecentActivity;
