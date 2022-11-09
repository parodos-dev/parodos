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
import MuiAccordion from '@material-ui/core/Accordion';
import Typography from '@material-ui/core/Typography';
import { Flex } from 'rebass';

export const Accordion = styled(MuiAccordion)`
  && {
    margin-left: 10px;
    width: 100%;
    background-color: #f6f8f2;
    padding: 15px 25px;
    border: 1px solid transparent;
    transition: 0.2s border-color;
    border-color: ${({ selected }) => (selected ? '#C0D554' : 'transparent')};

    &:hover {
      border-color: #c0d554;
    }
  }
`;

export const FullWidthFlexContainer = styled(Flex)`
  width: 100%;
`;

export const NotificationTitle = styled(Typography)`
  && {
    font-size: 14px;
    color: #2a2a2a;
    font-weight: ${({ bold }) => (bold ? '500' : '400')};
    font-family: 'Roboto', sans-serif;
  }
`;

export const NotificationText = styled(Typography)`
  && {
    font-size: 14px;
    color: #2a2a2a;
    font-weight: 400;
    font-family: 'Roboto', sans-serif;

    @media (max-width: 830px) {
      visibility: ${({ hideOnMobile }) =>
        hideOnMobile ? 'hidden' : 'visible'};
    }
  }
`;

export const AccordionDetailsContainer = styled.div`
  width: 100%;
  min-height: 200px;
`;

export const AccordionDetailsText = styled(Typography)`
  && {
    font-size: 14px;
    color: #2a2a2a;
    font-family: 'Roboto', sans-serif;
  }
`;
