import React from 'react';
import { createDevApp } from '@backstage/dev-utils';
import { InfrastructurePage, infrastructurePlugin } from '../src/plugin';

createDevApp()
  .registerPlugin(infrastructurePlugin)
  .addPage({
    element: <InfrastructurePage />,
    title: 'Root Page',
    path: '/infrastructure',
  })
  .render();
