import React from 'react';
import { createDevApp } from '@backstage/dev-utils';
import { TrainingPage, trainingPlugin } from '../src/plugin';

createDevApp()
  .registerPlugin(trainingPlugin)
  .addPage({
    element: <TrainingPage />,
    title: 'Root Page',
    path: '/training',
  })
  .render();
