# Move2Kube setup

**Step 1:**

Install all the manifests needed:

```
kubectl kustomize hack/manifests/move2kube | kubectl apply -f -
```

**Step 2**:


Wait until all pods are ready:

```
kubectl wait --namespace move2kube --for=condition=ready pod --all --timeout=600s
kubectl wait --namespace backstage --for=condition=ready pod --all --timeout=600s
kubectl wait --namespace default --for=condition=ready pod --all --timeout=600s
```

**Step 3**

Add the PubKeys to your private repo to be able to clone or write

```
cat hack/manifests/move2kube/keys/id_rsa.pub
```

**Step 4**

Init the move2kube intial setup:

```
kubectl exec -ti client -- /opt/config/init.sh
```


Step 5:

Here two options are available:

Option 1:

Run example script from terminal, but it's just good for testing:

```
kubectl exec -ti client -- /opt/config/test_move2kube.sh
```

Option 2:

Run the workflows using backstage.

```
kubectl port-forward --namespace=backstage svc/backstage 7007:7007
kubectl port-forward --namespace=move2kube svc/move2kube 8080:8080
```
