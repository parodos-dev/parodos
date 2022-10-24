import { deployPlugin } from './plugin';

describe('deploy', () => {
  it('should export plugin', () => {
    expect(deployPlugin).toBeDefined();
  });
});
