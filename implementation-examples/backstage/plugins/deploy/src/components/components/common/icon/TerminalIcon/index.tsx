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

import SvgIcon from '@material-ui/core/SvgIcon';
import React from 'react';

export const TerminalIcon = props => (
  <SvgIcon
    {...props}
    width="256px"
    height="256px"
    viewBox="0 0 256 256"
    id="Flat"
  >
    <path d="M72.50391,150.62988,100.791,128,72.50391,105.37012A11.9996,11.9996,0,0,1,87.49609,86.62988l40,32a11.99895,11.99895,0,0,1,0,18.74024l-40,32a11.9996,11.9996,0,1,1-14.99218-18.74024ZM143.99414,172h32a12,12,0,1,0,0-24h-32a12,12,0,0,0,0,24ZM236,56.48535v143.0293A20.50824,20.50824,0,0,1,215.51465,220H40.48535A20.50824,20.50824,0,0,1,20,199.51465V56.48535A20.50824,20.50824,0,0,1,40.48535,36h175.0293A20.50824,20.50824,0,0,1,236,56.48535ZM212,60H44V196H212Z" />
  </SvgIcon>
);
