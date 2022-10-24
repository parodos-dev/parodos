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

import styled from 'styled-components';
import { Typography } from '@material-ui/core';

export const Container = styled.div`
  width: 100%;
  margin-top: 40px;
`;

export const Title = styled(Typography)`
  && {
    font-family: 'Roboto', sans-serif;
    font-size: 20px;
    color: #2a2a2a;
    font-weight: 500;
    margin-bottom: 19px;
  }
`;

export const FullWidthTable = styled.table`
  width: 100%;
`;
