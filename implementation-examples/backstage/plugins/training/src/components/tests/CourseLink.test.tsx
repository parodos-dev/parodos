/*
 *  Copyright (c) 2022 Red Hat Developer
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
 *
 */

/**
 * @author Richard Wang (Github: RichardW98)
 */

import React from 'react';
import renderer from 'react-test-renderer';
import CourseLink from '../components/training/CourseLink';

describe('CourseLink', () => {
  const defaultProps = {
    academyRecommendation: {
      url: 'https://test.ca',
      title: 'Test Course Link',
    },
  };

  it('CourseLink should match stored snapshot', () => {
    const tree = renderer.create(<CourseLink {...defaultProps} />).toJSON();
    expect(tree).toMatchSnapshot();
  });

  it('CourseLink is an a tag with a url prop', () => {
    const tree = renderer.create(<CourseLink {...defaultProps} />).toJSON();
    expect(tree.type).toBe('a');
    expect(tree.props.target).toBe('_blank');
    expect(tree.props.href).toBe(defaultProps.academyRecommendation.url);
  });
});
