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

export const DangerIcon = props => (
  <SvgIcon
    {...props}
    height="18px"
    viewBox="0 0 20 20"
    width="18px"
    fill="#000000"
  >
    <path d="M0 0h20v20H0z" fill="none" />
    <path d="M13.3 2L18 6.7v6.6L13.3 18H6.7L2 13.3V6.7L6.7 2h6.6zm.7 5l-1-1-3 3-3-3-1 1 3 3-3 3 1 1 3-3 3 3 1-1-3-3 3-3z" />
  </SvgIcon>
);
