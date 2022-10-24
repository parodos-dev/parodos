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

import React, { useContext, useEffect } from 'react';
import * as R from 'ramda';

import AutoCompleteInput from '../../base/AutoCompleteInput';
import { TrainingContext } from '../../../context/training';
import useGetAllEmployees from '../../../hooks/useGetAllEmployees';

const EmployeeSearchInput = () => {
  const getAllEmployeesHook = useGetAllEmployees();
  const trainingContext = useContext(TrainingContext);

  useEffect(() => {
    getAllEmployeesHook.getAllEmployees();
  }, []);

  return (
    trainingContext.employeesState && (
      <AutoCompleteInput
        disabled={getAllEmployeesHook.isLoading}
        onChange={trainingContext.setCurrentEmployee}
        autoCompleteData={trainingContext.employeesState}
        getOptionLabel={option =>
          !R.isEmpty(option) ? `${option.name} (id: ${option.id})` : ''
        }
      />
    )
  );
};

export default EmployeeSearchInput;
