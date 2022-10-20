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
import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import Badge from '@material-ui/core/Badge';
import MailIcon from '@material-ui/icons/Mail';
import PersonIcon from '@material-ui/icons/Person';
import { Box, Divider } from '@material-ui/core';
import MenuItemList from './subCpnt/MenuItemList';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { configApiRef, useApi } from '@backstage/core-plugin-api';
import Grow from '@material-ui/core/Grow';
import Paper from '@material-ui/core/Paper';
import Popper from '@material-ui/core/Popper';
import MenuItem from '@material-ui/core/MenuItem';
import MenuList from '@material-ui/core/MenuList';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';

const useStyles = makeStyles(theme => ({
  grow: {
    flexGrow: 1,
  },
  menuButton: {
    marginRight: theme.spacing(2),
  },
  title: {
    fontSize: 22,
    fontFamily: `'Roboto Slab', serif`,
    color: theme.palette.primary.contrastText,
    marginRight: theme.spacing(8),
    display: 'block',
  },
  menuList: {
    position: 'relative',
    marginRight: theme.spacing(2),
    marginLeft: 0,
    width: '100%',
    [theme.breakpoints.up('sm')]: {
      marginLeft: theme.spacing(3),
      width: 'auto',
    },
  },
  divider: {
    backgroundColor: '#FFFFFF80',
    height: 32,
  },
}));

export default function AppbarTop({ logout }: any) {
  const classes = useStyles();
  const [messages, setMessages] = useState(0);
  const [open, setOpen] = React.useState(false);
  const anchorRef = React.useRef(null);
  const navigate = useNavigate();
  const config = useApi(configApiRef);

  const handleToggle = () => {
    setOpen(prevOpen => !prevOpen);
  };

  const handleClose = event => {
    if (anchorRef.current && anchorRef.current.contains(event.target)) {
      return;
    }

    setOpen(false);
  };

  function handleListKeyDown(event) {
    if (event.key === 'Tab') {
      event.preventDefault();
      setOpen(false);
    }
  }

  useEffect(() => {
    const url = config
      .getConfigArray('app.support.items')[1]
      .getConfigArray('links')[0]
      .getString('url');
    const interval = setInterval(async () => {
      try {
        await axios
          .get(`${url}/api/v1/notifications/unread`, {
            headers: {
              Authorization: 'Bearer ' + sessionStorage.getItem('access_token'),
            },
          })
          .then(messages => {
            setMessages(messages.data);
          });
      } catch (error) {
        setMessages(0);
      }
    }, 1000);
    return () => {
      clearInterval(interval);
    };
  }, []);

  return (
    <div className={classes.grow}>
      <AppBar position="static">
        <Toolbar>
          <Box display="flex" alignItems="center" width={260 - 24}>
            <Typography variant={'h5'} style={{ marginLeft: 65 }} noWrap>
              Parodos
            </Typography>
          </Box>
          <Divider orientation="vertical" className={classes.divider} />
          <MenuItemList />
          <div className={classes.grow} />
          <div>
            <IconButton
              aria-label="show new mails"
              color="inherit"
              onClick={() => navigate('/notification')}
            >
              <Badge
                overlap="rectangular"
                badgeContent={messages}
                color="secondary"
              >
                <MailIcon />
              </Badge>
            </IconButton>
            <IconButton
              aria-label="show new notifications"
              color="inherit"
              aria-controls={open ? 'menu-list-grow' : undefined}
              aria-haspopup="true"
              onClick={handleToggle}
              ref={anchorRef}
            >
              <PersonIcon />
            </IconButton>
            <Popper
              open={open}
              anchorEl={anchorRef.current}
              role={undefined}
              transition
              disablePortal
              style={{ zIndex: 9999 }}
            >
              {({ TransitionProps, placement }) => (
                <Grow
                  {...TransitionProps}
                  style={{
                    transformOrigin:
                      placement === 'bottom' ? 'center top' : 'center bottom',
                  }}
                >
                  <Paper>
                    <ClickAwayListener onClickAway={handleClose}>
                      <MenuList
                        autoFocusItem={open}
                        id="menu-list-grow"
                        onKeyDown={handleListKeyDown}
                      >
                        <MenuItem onClick={() => navigate('/settings')}>
                          Profile
                        </MenuItem>
                        <MenuItem onClick={logout}>Logout</MenuItem>
                      </MenuList>
                    </ClickAwayListener>
                  </Paper>
                </Grow>
              )}
            </Popper>
          </div>
        </Toolbar>
      </AppBar>
    </div>
  );
}
