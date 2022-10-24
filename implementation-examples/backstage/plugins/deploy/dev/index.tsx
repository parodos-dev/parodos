import React from 'react';
import { createDevApp } from '@backstage/dev-utils';
import { DeployPage, deployPlugin } from '../src';

createDevApp()
  .registerPlugin(deployPlugin)
  .addPage({
    element: <DeployPage />,
    title: 'Root Page',
    path: '/deploy',
  })
  .render();
