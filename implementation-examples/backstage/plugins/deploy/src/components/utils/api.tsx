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

import axios from 'axios';
// import {normalize} from 'normalizr';

const getAxiosInstance = ({ schema, baseUrl }) => {
  const transformResponse = schema && [schema];
  return axios.create({
    baseURL: baseUrl,
  });
};

// export const normalizeData = (schema) => (data) => {
//     try {
//         return normalize(JSON.parse(data), schema);
//     } catch (e) {
//         return data;
//     }
// };

export const requireId =
  request =>
  (idArg, ...restArgs) =>
    !idArg
      ? Promise.reject(
          new Error(
            'ID argument is required to make this request. Cancelling request.',
          ),
        )
      : request(idArg, ...restArgs);

export const get = (url: string, baseUrl: string, schema = null) => {
  const ax = getAxiosInstance({ schema, baseUrl });

  return ax.get(url, {
    headers: {
      Authorization: bearerAuth(sessionStorage.getItem('access_token')),
    },
  });
};

function bearerAuth(token) {
  return `Bearer ${token}`;
}

export const post = (url, body, token, schema = null) => {
  const ax = getAxiosInstance({ schema });
  return ax.post(url, body, {
    headers: {
      Authorization: bearerAuth(sessionStorage.getItem('access_token')),
    },
  });
};

export const patch = (url, body, schema = null) => {
  const ax = getAxiosInstance({ schema });
  return ax.patch(url, body);
};

export const put = (url, body, schema = null) => {
  const ax = getAxiosInstance({ schema });
  return ax.put(url, body);
};

export const destroy = (url, schema = null) => {
  const ax = getAxiosInstance({ schema });
  return ax.delete(url);
};
