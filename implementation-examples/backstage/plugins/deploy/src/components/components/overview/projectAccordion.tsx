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

import React, { useState } from 'react';
import {
  Accordion,
  AccordionDetails,
  Box,
  Button,
  Checkbox,
  Grid,
  Paper,
  Typography,
} from '@material-ui/core';
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import CancelOutlinedIcon from '@material-ui/icons/CancelOutlined';
import RotateRightOutlinedIcon from '@material-ui/icons/RotateRightOutlined';
import { useNavigate } from 'react-router-dom';
import { EProjectType } from '../../enum/projectType';
import { IconLeftAccordionSummary, useStyles } from './styles';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import AccordionDetail from './accordionDetail';

const ProjectAccordion = ({
  project,
  expanded,
  handleExpand,
  handleSelect,
}) => {
  const { repositoryName, organizationName, latestCommitDetail, type } =
    project;
  const [style, setStyle] = useState(false);
  const classes = useStyles();

  let navigate = useNavigate();

  const handleViewClick = () => {
    navigate(`/deploy/runDetail/projectId/${project.id}`, { state: project });
  };

  const setBoxStyle = (select: boolean) => {
    if (select) setStyle(true);
    else setStyle(false);
  };

  const getAvatar = (t: any) => {
    switch (t) {
      case EProjectType.COMPLETED:
        return <CheckCircleIcon color={'primary'} />;
      case EProjectType.ERROR:
        return <CancelOutlinedIcon color={'error'} />;
      case EProjectType.RUNNING:
        return <RotateRightOutlinedIcon />;
      default:
        return <Grid item />;
    }
  };

  return (
    <Grid
      container
      spacing={3}
      style={{ lineHeight: 5, marginBottom: 3, marginLeft: 10 }}
      className="project-card"
    >
      <Grid item xs={10}>
        <Box
          sx={{
            border: style ? 3 : 0,
            borderColor: 'primary.main',
            borderRadius: 2,
          }}
        >
          <Paper elevation={0} style={{ borderRadius: 10 }}>
            <Accordion
              TransitionProps={{ unmountOnExit: true }}
              expanded={expanded === project.id}
              onChange={handleExpand(project.id)}
            >
              <IconLeftAccordionSummary
                expandIcon={<ExpandMoreIcon />}
                aria-controls="panel1bh-content"
                id="panel1bh-header"
              >
                <Grid container>
                  <Grid
                    item
                    xs={1}
                    style={{ alignSelf: 'center', marginTop: 10 }}
                    mt={3}
                    pl={3}
                    height={50}
                  >
                    {getAvatar(type)}
                  </Grid>
                  <Grid item xs={3} style={{ alignSelf: 'center' }}>
                    <span>{organizationName + '/' + repositoryName}</span>
                  </Grid>
                  <Grid item xs={6} style={{ alignSelf: 'center' }}>
                    <Typography noWrap style={{ alignContent: 'center' }}>
                      {latestCommitDetail}
                    </Typography>
                  </Grid>
                  <Grid item xs={2} style={{ alignSelf: 'center' }}>
                    <Button onClick={handleViewClick} color={'secondary'}>
                      <Typography variant={'body1'}>VIEW</Typography>
                    </Button>
                  </Grid>
                </Grid>
              </IconLeftAccordionSummary>
              <AccordionDetails>
                <AccordionDetail project={project} />
              </AccordionDetails>
            </Accordion>
          </Paper>
        </Box>
      </Grid>
      <Grid item xs={2} style={{ alignSelf: 'center' }}>
        <Checkbox
          onChange={event => {
            setBoxStyle(event.target.checked);
            handleSelect(event.target.checked, project.id);
          }}
        />
      </Grid>
    </Grid>
  );
};

export default ProjectAccordion;
