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
import EnvFormLine from '../EnvFormLine';
import SimpleDialog from '../../../../../../../../common/dialog/SimpleDialog';
import SpanButton from '../../../../../../../../common/button/SpanButton';
import { BlackContainedButton } from '../../../../../../../../common/button/BlackContainedButton';
import { Box, Grid } from '@material-ui/core';
import { RunDetailContext } from '../../../../../../../../../contexts/projects/runDetail';

export default function ConfigureEnvDialog({
  handleClose,
  stageName,
  handleUpdated,
}) {
  const runDetailContext = useContext(RunDetailContext);
  const addVariable = () => {
    // dispatch(addEnvForm(stageName));
    const stageEnv = runDetailContext.env[stageName];
    stageEnv.push({ name: '', value: '' });
    runDetailContext.setEnv({ ...runDetailContext.env, [stageName]: stageEnv });
    console.log(runDetailContext.env);
  };
  useEffect(() => {
    if (!runDetailContext.env[stageName]) {
      console.log(runDetailContext.env[stageName]);
      runDetailContext.setEnv({
        ...runDetailContext.env,
        [stageName]: [{ name: '', value: '' }],
      });
      console.log(runDetailContext.env);
    }
  }, [runDetailContext.open]);

  const handleUpdate = () => {
    //NOTE API NEEDED
    handleUpdated();
    handleClose();
  };
  return (
    <SimpleDialog
      open={runDetailContext.open}
      title={`Configure Environment Variables for ${stageName.toUpperCase()}`}
      {...{ handleClose }}
    >
      <Box minWidth={450}>
        <Box minHeight={250} mt={3}>
          {runDetailContext.env[stageName] &&
            runDetailContext.env[stageName].length > 0 &&
            runDetailContext.env[stageName].map((el, index) => (
              <EnvFormLine
                key={index}
                {...{ stageName, name: el.name, value: el.value, index }}
              />
            ))}
          <Box mt={3}>
            <SpanButton label="add variables" handleClick={addVariable} />
          </Box>
        </Box>
        <Box mt={3}>
          <Grid container spacing={3} alignItems="center">
            <Grid item>
              <BlackContainedButton onClick={handleUpdate}>
                UPDATE
              </BlackContainedButton>
            </Grid>
            <Grid item>
              <SpanButton label="cancel" handleClick={handleClose} />
            </Grid>
          </Grid>
        </Box>
      </Box>
    </SimpleDialog>
  );
}
