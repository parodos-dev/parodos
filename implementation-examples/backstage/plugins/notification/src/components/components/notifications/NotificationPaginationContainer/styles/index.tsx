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
import { Flex } from 'rebass';

export const DesktopContainer = styled(Flex)`
  flex: 3;
  align-items: center;

  @media (max-width: 830px) {
    display: none;
    visibility: hidden;
  }
`;

export const MobileContainer = styled(Flex)`
  flex: 2;
  align-items: center;
  justify-content: space-between;
  visibility: hidden;
  flex-direction: column;

  @media (max-width: 830px) {
    visibility: visible;
  }
`;

export const FullWidthFlex = styled(Flex)`
  width: 100%;
`;
