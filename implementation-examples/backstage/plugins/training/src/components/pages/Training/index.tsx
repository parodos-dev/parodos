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

import Title from '../../components/base/Title';
import EmployeeSearchInput from '../../components/training/EmployeeSearchInput';
import RecentActivity from '../../components/training/RecentActivity';
import HistoryTable from '../../components/training/HistoryTable';

import * as Styled from '../../styles';

const Training = () => {
  return (
    <Styled.PageWrapper>
      <Title variant="h1" paragraph>
        Training
      </Title>
      <Flex mb="40px">
        <Title fontSize="16px" fontWeight="300">
          Review developer performance and training recommendations
        </Title>
      </Flex>
      <EmployeeSearchInput />
      <RecentActivity />
      <HistoryTable />
    </Styled.PageWrapper>
  );
};

export default Training;
