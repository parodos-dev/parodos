/*
 * Copyright 2020 The Backstage Authors
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
 */

import React, { PropsWithChildren, useEffect, useState } from 'react';
import {
  Box,
  CircularProgress,
  Grid,
  Link,
  makeStyles,
} from '@material-ui/core';
import ExtensionIcon from '@material-ui/icons/Extension';
import LibraryBooks from '@material-ui/icons/LibraryBooks';
import { Sidebar } from '@backstage/core-components';
import { NavLink } from 'react-router-dom';
import {
  Settings as SidebarSettings,
  UserSettingsSignInAvatar,
} from '@backstage/plugin-user-settings';
import {
  sidebarConfig,
  SidebarDivider,
  SidebarGroup,
  SidebarItem,
  SidebarPage,
  SidebarScrollWrapper,
  SidebarSpace,
  useSidebarOpenState,
} from '@backstage/core-components';
import MenuIcon from '@material-ui/icons/Menu';
import SyncAltIcon from '@material-ui/icons/SyncAlt';
import MailIcon from '@material-ui/icons/Mail';
import App from '../navbar/App';
import Keycloak from 'keycloak-js';
import { useApi, configApiRef } from '@backstage/core-plugin-api';
import LogoFull from './LogoFull';
import LogoIcon from './LogoIcon';

const useSidebarLogoStyles = makeStyles({
  root: {
    width: sidebarConfig.drawerWidthClosed,
    height: 3 * sidebarConfig.logoHeight,
    display: 'flex',
    flexFlow: 'row nowrap',
    alignItems: 'center',
    marginBottom: -14,
  },
  link: {
    width: sidebarConfig.drawerWidthClosed,
    marginLeft: 24,
  },
});

const SidebarLogo = () => {
  const classes = useSidebarLogoStyles();
  const { isOpen } = useSidebarOpenState();

  return (
    <div className={classes.root} style={{ marginLeft: -5, marginTop: -15 }}>
      <Link
        component={NavLink}
        to="/"
        underline="none"
        className={classes.link}
        aria-label="Home"
      >
        {isOpen ? <LogoFull /> : <LogoIcon />}
      </Link>
    </div>
  );
};

const RootApp = ({ children, logout }) => {
  return (
    <Box>
      <App {...{ logout }} />
      <SidebarPage>
        <Sidebar>
          <SidebarLogo />
          <SidebarDivider />
          <SidebarGroup label="Menu" icon={<MenuIcon />}>
            <SidebarItem icon={ExtensionIcon} to="deploy" text="Projects" />
            <SidebarItem
              icon={SyncAltIcon}
              to="infrastructure"
              text="Infrastructure"
            />
            <SidebarItem icon={LibraryBooks} to="training" text="Training" />
            {/* End global nav */}
            <SidebarDivider />
            <SidebarScrollWrapper>
              <SidebarItem
                icon={MailIcon}
                to="notification"
                text="Notification"
              />
            </SidebarScrollWrapper>
          </SidebarGroup>
          <SidebarSpace />
          <SidebarDivider />
          <SidebarGroup
            label="Settings"
            icon={<UserSettingsSignInAvatar />}
            to="/settings"
          >
            <SidebarSettings />
          </SidebarGroup>
        </Sidebar>
        {children}
      </SidebarPage>
    </Box>
  );
};

export const Root = ({ children }: PropsWithChildren<{}>) => {
  const [authenticated, setAuthenticated] = useState<boolean>(false);
  const [keycloak, setKeycloak] = useState<Keycloak | any>();

  const config = useApi(configApiRef);

  useEffect(() => {
    const keycloakUrl = config
      .getConfigArray('app.support.items')[0]
      .getConfigArray('links')[0]
      .getString('url');
    console.log('keycloak url:', keycloakUrl);
    let initOptions = {
      url: keycloakUrl,
      realm: 'Parodos',
      clientId: 'container',
    };

    let keycloak: Keycloak = new Keycloak(initOptions);
    setKeycloak(keycloak);
    keycloak
      .init({ onLoad: 'login-required' })
      .then(authenticated => {
        if (authenticated) {
          sessionStorage.setItem(
            'access_token',
            keycloak.token ? keycloak.token : '',
          );
          setAuthenticated(authenticated);

          sessionStorage.setItem(
            'user_name',
            keycloak.tokenParsed?.preferred_username,
          );
        }
      })
      .catch(e => {
        console.error('Authenticated Failed: ' + e);
      });
  }, []);

  function logout() {
    console.log(keycloak);
    const logoutOptions = { redirectUri: 'https://github.com/logout' };
    keycloak.logout(logoutOptions).then((success: any) => {
      console.log('logout success!', success);
      sessionStorage.setItem('access_token', '');
    });
  }

  return !authenticated ? (
    <Grid
      item
      style={{
        width: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        paddingTop: '20%',
      }}
    >
      <CircularProgress color="secondary" size={100} />
    </Grid>
  ) : (
    <RootApp {...{ logout }}>{children}</RootApp>
  );
};
