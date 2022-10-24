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
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import { useStyles } from './styles';
import { TextInput } from '../../../../../../../../common/form/TextInput';
import { Grid, IconButton } from '@material-ui/core';
import { RunDetailContext } from '../../../../../../../../../contexts/projects/runDetail';

export default function EnvFormLine({ stageName, name, value, index }) {
  const classes = useStyles();
  const runDetailContext = useContext(RunDetailContext);

  const handleDelete = () => {
    const envs = runDetailContext.env;
    envs[stageName].splice(index, 1);
    runDetailContext.setEnv(envs);
  };
  const handleChangeName = e => {
    let envFirst = runDetailContext.env[stageName];
    envFirst[index].name = e.target.value;
    runDetailContext.setEnv({ ...runDetailContext.env, [stageName]: envFirst });
  };
  const handleChangeValue = e => {
    let envSecond = runDetailContext.env[stageName];
    envSecond[index].value = e.target.value;
    runDetailContext.setEnv({
      ...runDetailContext.env,
      [stageName]: envSecond,
    });
  };

  return (
    <Grid container alignItems="flex-end" spacing={2} mb={3}>
      <Grid item>
        <TextInput
          size="small"
          value={name}
          label="Name"
          onChange={handleChangeName}
        />
      </Grid>
      <Grid item>
        <TextInput
          size="small"
          label="Value"
          value={value}
          onChange={handleChangeValue}
        />
      </Grid>
      {index !== 0 && (
        <Grid item>
          <IconButton onClick={handleDelete} className={classes.removeBtn}>
            <DeleteForeverIcon />
          </IconButton>
        </Grid>
      )}
    </Grid>
  );
}
