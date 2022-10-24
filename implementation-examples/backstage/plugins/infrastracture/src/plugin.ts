import {
  createPlugin,
  createRoutableExtension,
} from '@backstage/core-plugin-api';

import { rootRouteRef } from './routes';

export const infrastructurePlugin = createPlugin({
  id: 'infrastructure',
  routes: {
    root: rootRouteRef,
  },
});

export const InfrastructurePage = infrastructurePlugin.provide(
  createRoutableExtension({
    name: 'InfrastructurePage',
    component: () => import('./components/App').then(m => m.ProviderWrappedApp),
    mountPoint: rootRouteRef,
  }),
);
