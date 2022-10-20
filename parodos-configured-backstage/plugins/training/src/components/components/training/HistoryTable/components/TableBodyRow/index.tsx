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
import PropTypes from 'prop-types';
import * as R from 'ramda';

import CourseLink from '../../../CourseLink';

import * as Styled from '../../styles';

const TableBodyRow = ({ employeeHistoryItem }) => {
  return (
    <React.Fragment>
      <Styled.CustomTableRow>
        <Styled.NoBorderTableCell>
          {moment(employeeHistoryItem.dateOfAssignment).format('MMM DD YYYY')}
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          <CourseLink
            academyRecommendation={employeeHistoryItem.academyRecommendation}
          />
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          {employeeHistoryItem.assignmentTrigger}
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          {employeeHistoryItem.progress}
        </Styled.NoBorderTableCell>
        <Styled.NoBorderTableCell>
          {R.is(Number, employeeHistoryItem.currentStatus)
            ? `${employeeHistoryItem.currentStatus}%`
            : employeeHistoryItem.currentStatus}
        </Styled.NoBorderTableCell>
      </Styled.CustomTableRow>
      <tr />
    </React.Fragment>
  );
};

TableBodyRow.propTypes = {
  employeeHistoryItem: PropTypes.shape({
    id: PropTypes.string.isRequired,
    dateOfAssignment: PropTypes.number.isRequired,
    academyRecommendation: PropTypes.shape({}).isRequired,
    assignmentTrigger: PropTypes.string.isRequired,
    progress: PropTypes.string.isRequired,
    currentStatus: PropTypes.oneOfType([
      PropTypes.number.isRequired,
      PropTypes.string.isRequired,
    ]),
  }).isRequired,
};

export default TableBodyRow;
