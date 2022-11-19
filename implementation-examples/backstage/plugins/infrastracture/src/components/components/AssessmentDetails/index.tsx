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
 * @author Luke Shannon (Github: lshannon)
 */

import React, { useEffect, useState } from 'react';
import { Box, Button, Grid, TextField, Typography } from '@material-ui/core';
import { Flex } from 'rebass';
import useGetWorkFlowParams from '../../hooks/useGetWorkFlowParams';
import { constants } from '../../util/constant';

type Props = {
  setCurrentStepState: (step: number) => void | any;
  setFirstStep: (step: number) => void | any;
  setAssessmentParams: (params: { [key: string]: string }) => void | any;
};

const AssessmentDetails = (props: Props) => {
  const getAssessmentParamsHook = useGetWorkFlowParams();
  const [params, setParams] = useState<{ [key: string]: string }>({});
  const [formValid, setFormValid] = useState<boolean | any>(false);

  useEffect(() => {
    getAssessmentParamsHook
      .getWorkFlowParams({
        workflow: constants.ASSESSMENT_WORKFLOW,
        workflowName: constants.ASSESSMENT_WORKFLOW_NAME,
      })
      .then(paramList => {
        if (paramList.length === 0) {
          props.setCurrentStepState(1);
          props.setFirstStep(1);
        }
      });
  }, []);

  const handleParamOnChange = (
    event: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>,
    name: string,
  ) => {
    setFormValid(event.target.form?.reportValidity());
    setParams({
      ...params,
      [name]: event.target.value,
    });
  };

  return (
    <div>
      <Typography paragraph>
        <b>Assessment Parameters</b>
      </Typography>
      <Box>
        <form>
          {getAssessmentParamsHook.workFlowParams.map((param, index) => (
            <Box key={`param_${index}`}>
              <Grid item md={4}>
                <TextField
                  required={!param.optional}
                  style={{ width: '100%', marginBottom: 20 }}
                  id={`param_${index}`}
                  label={param.key}
                  helperText={param.description}
                  type={param.type.toLowerCase()}
                  onChange={event => handleParamOnChange(event, param.key)}
                />
                <span className="validity"></span>
              </Grid>
            </Box>
          ))}
        </form>
      </Box>
      <Flex justifyContent="center" mt="100px">
        <Flex ml="20px">
          <Button
            style={{}}
            variant="contained"
            onClick={() => {
              props.setAssessmentParams(params);
              props.setCurrentStepState(1);
            }}
            disabled={!formValid}
          >
            Next
          </Button>
        </Flex>
      </Flex>
    </div>
  );
};

export default AssessmentDetails;
