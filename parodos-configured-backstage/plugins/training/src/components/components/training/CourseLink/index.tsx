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
import PropTypes from 'prop-types';
import { Tooltip } from '@material-ui/core';

import * as Styled from './styles';

const CourseLink = ({ academyRecommendation }) => {
  return (
    <Styled.CourseLinkAnchor
      href={academyRecommendation.url}
      target="_blank"
      rel="noreferrer"
    >
      {academyRecommendation.title}
      <Tooltip title="View in Academy">
        <Styled.NewTabIcon />
      </Tooltip>
    </Styled.CourseLinkAnchor>
  );
};

CourseLink.propTypes = {
  academyRecommendation: PropTypes.shape({
    url: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
  }).isRequired,
};

export default CourseLink;
