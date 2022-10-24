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
import moment from 'moment';
import * as R from 'ramda';
import CourseLink from '../../CourseLink';

const recentActivityConfig = {
  eventName: {
    label: 'EVENT NAME',
    dataKey: 'eventName',
  },
  dateOfAssignment: {
    label: 'DATE OF ASSIGNMENT',
    dataKey: 'dateOfAssignment',
    formatFunction: date => {
      if (R.is(Number, date)) {
        return moment(date).format('MMM DD YYYY');
      }
      // return '- -';
      return moment(date).format('MMM DD YYYY');
    },
  },
  assignmentTrigger: {
    label: 'ASSIGNMENT TRIGGER',
    dataKey: 'assignmentTrigger',
  },
  initialEventPoint: {
    label: 'INITIAL EVENT POINT',
    dataKey: 'initialEventPoint',
    formatFunction: initialEventPoint => {
      if (R.is(Number, initialEventPoint)) {
        return `${initialEventPoint}%`;
      }
      return initialEventPoint;
    },
  },
  academyRecommendation: {
    label: 'ACADEMY RECOMMENDATION',
    dataKey: 'academyRecommendation',
    formatFunction: academyRecommendation => {
      if (academyRecommendation !== '- -') {
        return <CourseLink academyRecommendation={academyRecommendation} />;
      }
      return academyRecommendation;
    },
  },
  progress: {
    label: 'PROGRESS',
    dataKey: 'progress',
  },
  currentStatus: {
    label: 'CURRENT STATUS',
    dataKey: 'currentStatus',
    formatFunction: currentStatus => {
      if (R.is(Number, currentStatus)) {
        return `${currentStatus}%`;
      }
      return currentStatus;
    },
  },
};

export default recentActivityConfig;
