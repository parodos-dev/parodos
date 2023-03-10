# Install parados on plain kubernetes


## Create kind cluster

```
$ --> kind create cluster
Creating cluster "kind" ...
 ✓ Ensuring node image (kindest/node:v1.21.1) 🖼
 ✓ Preparing nodes 📦
 ✓ Writing configuration 📜
⢎⠁ Starting control-plane 🕹️
 ✓ Starting control-plane 🕹️
 ✓ Installing CNI 🔌
 ✓ Installing StorageClass 💾
Set kubectl context to "kind-kind"
You can now use your cluster with:

kubectl cluster-info --context kind-kind

Have a nice day! 👋
```

## Install backstage and sample parodos server:

Backstage with orion frontend app

```
$ --> kubectl kustomize hack/manifests/backstage  | kubectl apply -f -
namespace/backstage unchanged
configmap/app-config-ghg2dm7dd9 unchanged
secret/postgres-secrets-gh7c66h9g8 unchanged
service/backstage unchanged
service/postgres unchanged
service/notification-service unchanged
service/workflow-service unchanged
deployment.apps/backstage unchanged
deployment.apps/postgres unchanged
deployment.apps/notification-service unchanged
deployment.apps/workflow-service unchanged
```

## Backstage port-forwarding

Because there is no domain, to test without domains, we neeed to forward a few
ports to localhost, in the near future we'll figure it out a solution with
gateway-api.

```
kubectl port-forward --namespace=backstage svc/backstage 7007:7007
```

Now, start adding workflows using `http://localhost:7007/parodos/`
