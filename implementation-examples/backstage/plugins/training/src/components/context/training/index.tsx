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

import React, { createContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import * as R from 'ramda';

import useGetEmployeeRecentActivity from '../../hooks/useGetEmployeeRecentActivity';
import useGetEmployeeHistory from '../../hooks/useGetEmployeeHistory';

export const TrainingContext = createContext();

const TrainingProvider = ({ children }) => {
  const getEmployeeRecentActivityHook = useGetEmployeeRecentActivity();
  const getEmployeeHistoryHook = useGetEmployeeHistory();
  const [currentEmployeeState, setCurrentEmployeeState] = useState({});
  const [employeeRecentActivityState, setEmployeeRecentActivityState] =
    useState({});
  const [employeeHistoryState, setEmployeeHistoryState] = useState([]);
  const [employeesState, setEmployeesState] = useState([]);

  const currentEmployeeId = R.path(['id'], currentEmployeeState);

  useEffect(() => {
    if (currentEmployeeId) {
      Promise.all([
        getEmployeeRecentActivityHook.getEmployeeRecentActivity({
          setEmployeeRecentActivity: setEmployeeRecentActivityState,
          currentEmployeeId,
        }),
        getEmployeeHistoryHook.getEmployeeHistory({
          setEmployeeHistory: setEmployeeHistoryState,
          currentEmployeeId,
        }),
      ]);
    } else {
      setEmployeeRecentActivityState({});
      setEmployeeHistoryState([]);
    }
  }, [currentEmployeeId]);

  return (
    <TrainingContext.Provider
      value={{
        employeesState,
        setEmployeesState,
        currentEmployee: currentEmployeeState,
        setCurrentEmployee: setCurrentEmployeeState,
        employeeRecentActivity: employeeRecentActivityState,
        setEmployeeRecentActivity: setEmployeeRecentActivityState,
        employeeHistory: employeeHistoryState,
        setEmployeeHistory: setEmployeeHistoryState,
        currentEmployeeId,
      }}
    >
      {children}
    </TrainingContext.Provider>
  );
};

TrainingProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export default TrainingProvider;
