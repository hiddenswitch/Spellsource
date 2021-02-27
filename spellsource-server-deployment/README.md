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