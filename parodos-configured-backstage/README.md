# Parodos Backstage Configuration

The user interfaces for the Parodos workflows can be ran as Backstage plugins. Using Backstage provides an excellent means of transitioning between Parodos workflows creating a seamless client experience while consuming Parodos workflows.

The following is a high level summary of the Parodos and Backstage integration:

![architecture](./readme-images/backstage-configured-parodos.png) backstage-configured-parodos

The following outlines this integration in detail.

To learn more about Backstage.io, please refer to their documentation:
https://backstage.io/docs/overview/what-is-backstage

## Running Locally

In this directory is a Backstage distribution configured to run the Parodos workflows as Plugins.

***Note:*** This is an older version of Backstage. Efforts will be made to update it at a later time

To get started with Backstage refer to the project Prerequisite : https://backstage.io/docs/getting-started/#prerequisites

To run this Backstage install locally, execute the following commands:

```sh

yarn install
yarn tsc || true
source local-demo.sh
yarn build
yarn dev

```

***Note:*** A running instance of Keycloak is required to run this locally. How keycloak is configured is detailed below

## Building A Docker Image

To run the Parodos Configured Backstage an image will need to be generated.

***Note:*** Anytime a plugin is changed, or configuration file updated the Backstage image will need to be recreated. This is an obvious limitation once the application is in production with users consuming multiple workflows, however it is a limitation of Backstage itself and not something that can address by Parodos

To generate the image, run the following command 

```shell

docker image build . -f packages/backend/Dockerfile --tag backstage
docker run -it -p 9000:9000 -p 7007:7007 backstage

```

### Parodos Plugins

The Parodos configured backstage runs 5 Parodos plugins:

- Infrastructure
- Project View
- Notification
- Deploy
- Training

Each of these plugins is a relatively independent React application which has its own package.json, theme, context state and sub routes. The source of each plugin can be found in parodos-configured-backstage/plugins. For descriptions of what each of these workflows do, please refer to the main page of the Parodos project.

The following is a description of the Backstage customizations done to support these plugins.

### Authentication

Parodos uses Keycloak (https://www.keycloak.org/) as its Authentication provider. The provides Parodos a convient way to integrate with existing indentity providers and have a common pattern for validated authentication sessions across Parodos's components.

For more details on Keycloak:
https://www.keycloak.org/guides#getting-started

How Backstage is configured to use Keycloak explained in the following sections.

### Root App

Root App is a React Application running within the parodos-configured-backstage/packages/app folder of Backstage that intergrates the Parodos plugins and customizes Backstage for Parodos. 

The Root App (path: parodos-configured-backstage/packages/app/src) contains:

  - Navigation bar & Side bar
  - UI Theme
  - KeyCloak Authentication

  All Plugins Routes are registered in Root UI App.tsx and referred in Navigation Bar & Side Bar for routing purpose.

- plugin backends (Parodos plugins do not need any DB connection or any backend services, so this aspect is not required)

#### app-config.yaml

In parodos-configured-backstage/app-config.yaml the routing is configured for the plugins as well as Keycloak (authentication)

```yaml
app:
  title: Parodos
  baseUrl: ${APP_URL}
  support:
    url: ${APP_URL}
    items:
      - title: keycloak
        links:
          - url: ${KEYCLOAK_URL}
      - title: notification
        links:
          - url: ${NOTIFICATION_URL}
      - title: deploy
        links:
          - url: ${DEPLOY_URL}
      - title: migrate
        links:
          - url: ${MIGRATE_URL}
      - title: event
        links:
          - url: ${EVENT_URL}
```

1. ${APP_URL} is the host url of this backstage app
2. ${KEYCLOAK_URL} is the host url of Keycloak for the authentication
3. other ${***_URL} is the host url of the backend api to fetch data for this ui. e.g. ${DEPLOY_URL} is the api url of deploy service.

These values will be determined based on how these components are deploy. An example of the above configuration can be found in ./local-demo.sh

### Keycloak Configuration

GitHub is used as the identity provider of KeyCloak in the Parodos demo. To configure KeyCloak for integration with that Parodos configured Backstage, please follow these steps:

1. Go to the identity provider of the realm and create 'github' indendity provider
   ![step1](./readme-images/kc-1.png)
2. Copy the redirect URI
   ![step2](./readme-images/kc-2.png)
3. In GitHub, go to Settings/Developer settings and click "new OAuth app", and paste the value from step2 to "Authorization callback URL", then paste the same value in "Homepage URL" and cut "/broker/github/endpoint" at the end
4. In Github/Settings/Developer click "Register Application"  ![step3](./readme-images/kc-3.png)
4. In Github/Settings/Developer click "Generate a new client secret" and copy the value ![step4](./readme-images/kc-4.png)
5. Go back to Keycloak and add the "client secret" and "client Id", then save![step5](./readme-images/kc-5.png)
6. Go to Keycloak/Clients and create a new Client ![step6](./readme-images/kc-6.png)
7. Add the redirect Urls and Web Origins, the redirect Urls should contain "https://github.com/logout" ![step7](./readme-images/kc-7.png)
8. Go to Keycloak Authentication and click "Config" in the dropdown of Actions on line of "Identity Provider Redirector" ![step8](./readme-images/kc-8.png)
9. Fill in the name of the identity provider for github, then save ![step9](./readme-images/kc-9.png)

The Parodos realm and client is configured in [keycloak config](./packages/app/src/components/Root/Root.tsx?#L149-L150)

```javascript
...
    let initOptions = {
      url: keycloakUrl,
      realm: '****',
      clientId: '****',
    };
...
```

## Authors

Richard Wang (ricwang@redhat.com)
Luke Shannon (lshannon@redhat.com)
