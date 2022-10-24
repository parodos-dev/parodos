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

import React, { useContext } from 'react';
import PropTypes from 'prop-types';

import { TrainingContext } from '../../../../../context/training';

import * as TableRowUtils from './utils';
import * as Styled from './styles';

const TableRow = ({ label, dataKey, formatFunction }) => {
  const trainingContext = useContext(TrainingContext);

  return (
    <tr>
      <Styled.TableCellPaddingBottom isLabel>
        <Styled.TableText>{label}</Styled.TableText>
      </Styled.TableCellPaddingBottom>
      <Styled.TableCellPaddingBottom>
        <Styled.TableText>
          {TableRowUtils.getValueForTableCell(
            trainingContext,
            dataKey,
            formatFunction,
          )}
        </Styled.TableText>
      </Styled.TableCellPaddingBottom>
    </tr>
  );
};

TableRow.propTypes = {
  label: PropTypes.string.isRequired,
  dataKey: PropTypes.string.isRequired,
  formatFunction: PropTypes.oneOfType([PropTypes.func, PropTypes.oneOf([])]),
};

TableRow.defaultProps = {
  formatFunction: null,
};

export default TableRow;
