import {
  createPlugin,
  createRoutableExtension,
} from '@backstage/core-plugin-api';

import { rootRouteRef } from './routes';

export const deployPlugin = createPlugin({
  id: 'deploy',
  routes: {
    root: rootRouteRef,
  },
});

export const DeployPage = deployPlugin.provide(
  createRoutableExtension({
    name: 'DeployPage',
    component: () => import('./components/App').then(m => m.ProviderWrappedApp),
    mountPoint: rootRouteRef,
  }),
);
