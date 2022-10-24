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
import makeStyles from '@material-ui/core/styles/makeStyles';
import { Badge, Box } from '@material-ui/core';
import clsx from 'clsx';
import { useNavigate } from 'react-router-dom';

const useStyles = makeStyles(theme => ({
  menuList: {
    position: 'relative',
    marginRight: theme.spacing(2),
    marginLeft: 0,
    width: 'auto',
  },
  menuItem: {
    fontWeight: 500,
    cursor: 'pointer',
    color: theme.palette.text.secondary,
    marginLeft: theme.spacing(7),
    fontFamily: "'Roboto', sans-serif",
  },
  selected: {
    color: theme.palette.primary.contrastText,
  },
  badge: { right: -10 },
}));
const CustomMenuItem = ({ children, handleClick, link, classes, dirty }) => {
  let match = true; //useRouteMatch(link);

  return (
    <Badge
      overlap="rectangular"
      classes={{
        anchorOriginTopRightRectangular: classes.badge,
      }}
      variant="dot"
      invisible={!dirty}
      color="error"
    >
      <span
        className={clsx(classes.menuItem, match && classes.selected)}
        onClick={handleClick}
      >
        {children}
      </span>
    </Badge>
  );
};

const routers = [
  { routerlink: '/deploy', display: 'Projects', link: '/deploy' },
  { routerlink: '/training', display: 'Training', link: '/training' },
  {
    routerlink: '/infrastructure',
    display: 'Infrastructure',
    link: '/infrastructure',
  },
];

export default function MenuItemList() {
  const classes = useStyles();
  let navigate = useNavigate();
  const [dirtyIndicator, setDirtyIndicator] = useState(false);
  const handleClick = link => {
    navigate(link);
  };
  useEffect(() => {
    window.addEventListener('dirty', (e: any) => {
      setDirtyIndicator(e.detail);
    });
  }, []);
  return (
    <Box className={classes.menuList}>
      {routers.map((item, index) => (
        <CustomMenuItem
          dirty={index === 1 && dirtyIndicator}
          key={item.link}
          classes={classes}
          handleClick={() => handleClick(item.link)}
          link={item.link}
        >
          {item.display}
        </CustomMenuItem>
      ))}
    </Box>
  );
}
