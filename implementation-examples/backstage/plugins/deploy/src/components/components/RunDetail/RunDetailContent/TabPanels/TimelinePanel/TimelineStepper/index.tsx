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

import { getTimelineStatus } from './helper/getTimelineStatus';
import TimelineCard from './TimelineCard';
import { useStyles } from './styles';
import Timeline from '@material-ui/lab/Timeline';
import TimelineItem from '@material-ui/lab/TimelineItem';
import TimelineSeparator from '@material-ui/lab/TimelineSeparator';
import TimelineContent from '@material-ui/lab/TimelineContent';
import TimelineDot from '@material-ui/lab/TimelineDot';
import { CssTimelineConnector } from './TimelineCard/styles';

export default function TimelineStepper({ timeline }) {
  const classes = useStyles();
  return (
    <Timeline className={classes.root}>
      {timeline?.map((el, index) => (
        <TimelineItem
          key={index}
          classes={{
            missingOppositeContent: classes.missingOpposite,
          }}
        >
          <TimelineSeparator>
            <TimelineDot
              color="inherit"
              variant={'outlined'}
              className={classes.timelineDot}
            >
              {getTimelineStatus(el.status)}
            </TimelineDot>
            {index !== timeline.length - 1 && <CssTimelineConnector />}
          </TimelineSeparator>
          <TimelineContent>
            <TimelineCard {...el} />
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
}
