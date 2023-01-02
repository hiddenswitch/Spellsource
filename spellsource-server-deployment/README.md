# spellsource-server-deployment

## Preview Helm Chart

```shell
helm install --dry-run --debug --generate-name ./src/main/helm/spellsource
```

## Testing Locally (macOS)

 1. Install dependencies.
    ```shell
    brew install docker minikube helm
    ```
 2. Create an ingress tunnel.
    ```shell
    minikube start --vm=true # or minikube start --vm=true --driver=hyperkit
    minikube addons enable ingress registry
    ```
 3. Create a tunnel.
    ```shell
    minikube tunnel
    ```
 4. Retrieve the service IPs.
    ```shell
    kubectl get svc
    ```
    
Creating an EKS cluster:
```shell
eksctl create cluster --name hiddenswitch-cluster-1 --version 1.19 --fargate
```

### Using images from `ghcr.io`

Create a personal access token with `packages:read` permissions. Then create a manifest for the secret and reference it:

```yaml
---
apiVersion: v1
kind: Secret
metadata:
  name: image-pull-secret
stringData:
  .dockerconfigjson: '{"auths":{"ghcr.io":{"auth":"yourusername:ghp_generatedtoken"}}}'
```

Then, in your `values.yaml`:

```yaml
imagePullSecrets:
 - name: image-pull-secret
```