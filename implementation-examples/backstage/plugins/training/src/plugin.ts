import {
  createPlugin,
  createRoutableExtension,
} from '@backstage/core-plugin-api';

import { rootRouteRef } from './routes';

export const trainingPlugin = createPlugin({
  id: 'training',
  routes: {
    root: rootRouteRef,
  },
});

export const TrainingPage = trainingPlugin.provide(
  createRoutableExtension({
    name: 'TrainingPage',
    component: () => import('./components/App').then(m => m.ProviderWrappedApp),
    mountPoint: rootRouteRef,
  }),
);
