import React from 'react';
import { createDevApp } from '@backstage/dev-utils';
import { MigratePage, migratePlugin } from '../src/plugin';

createDevApp()
  .registerPlugin(migratePlugin)
  .addPage({
    element: <MigratePage />,
    title: 'Root Page',
    path: '/migrate',
  })
  .render();
