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
import { RunDetailContext } from '../../../../contexts/projects/runDetail';
import { Box, Grid, NativeSelect, Typography } from '@material-ui/core';
import { TerminalIcon } from '../../../common/icon/TerminalIcon';

export default function RunDetailHeader() {
  const runDetailContext = useContext(RunDetailContext);

  useEffect(() => {
    // runDetailContext.setSelectedBranch(mockBranchList[0]);
  }, []);

  const handleChange = event => {
    console.log('set branch: ', event.target.value);
    runDetailContext.setSelectedBranch(event.target.value);
  };

  const handleClick = event => {
    runDetailContext.setOpenLog(!runDetailContext.openLog);
  };

  return (
    <Grid container alignItems="center" spacing={8}>
      <Grid item>
        <Typography variant={'h4'}>
          {runDetailContext.selectedProject?.organizationName +
            '/' +
            runDetailContext.selectedProject?.repositoryName}
        </Typography>
      </Grid>
      <Grid item>
        <Box width="140px">
          <NativeSelect
            id="select-branch"
            onChange={handleChange}
            value={
              runDetailContext.selectedBranch
                ? runDetailContext.selectedBranch
                : runDetailContext.branches[0]
            }
            fullWidth
          >
            {runDetailContext.branches.map(b => (
              <option value={b} key={b}>
                {b}
              </option>
            ))}
          </NativeSelect>
        </Box>
      </Grid>
      <Grid item>
        <TerminalIcon style={{ marginLeft: 10 }} onClick={handleClick} />
      </Grid>
    </Grid>
  );
}
