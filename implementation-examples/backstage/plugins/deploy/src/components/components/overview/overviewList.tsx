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

import React, { useContext, useState } from 'react';
import { Box } from '@material-ui/core';
import { ProjectsContext } from '../../contexts/projects/projects';
import ProjectCard from './projectCard';
import ProjectAccordion from './projectAccordion';

const OverviewList = ({ handleSelect }) => {
  const [expanded, setExpanded] = useState<string | false>(false);
  const projectsContext = useContext(ProjectsContext);
  const handleExpand = panel => (event, isExpanded) => {
    setExpanded(isExpanded ? panel : false);
  };

  return (
    <Box width={'98%'}>
      {projectsContext.allProjects.map(p => {
        return sessionStorage.getItem('user_name') === 'lshannon' ? (
          <Box key={p.id} m={1}>
            <ProjectAccordion
              project={p}
              expanded={expanded}
              handleExpand={handleExpand}
              handleSelect={handleSelect}
            />
          </Box>
        ) : (
          <Box key={p.id} m={1}>
            <ProjectCard project={p} handleSelect={handleSelect} />
          </Box>
        );
      })}
    </Box>
  );
};

export default OverviewList;
