import React from 'react';
import { createDevApp } from '@backstage/dev-utils';
import { NotificationPage, notificationPlugin } from '../src/plugin';

createDevApp()
  .registerPlugin(notificationPlugin)
  .addPage({
    element: <NotificationPage />,
    title: 'Root Page',
    path: '/notification',
  })
  .render();
