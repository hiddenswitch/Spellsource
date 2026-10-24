FROM node:26.12.1-bookworm-slim@sha256:28147155d47dc31abe0a46e0d12b59b2fb3d0a2dcc5dc65fce76a074180b16fe
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates python3 make g++ \
 && update-ca-certificates \
 && rm -rf /var/lib/apt/lists/* \
 && npm install --global corepack@0.36.0 \
 && corepack enable
COPY ./ /spellsource/
WORKDIR /spellsource/spellsource-graphql/
RUN node /spellsource/.yarn/verify-trust.cjs /etc/ssl/certs/ca-certificates.crt \
 && yarn install --immutable \
 && yarn build \
 && yarn cache clean
EXPOSE 5678
ENTRYPOINT ["yarn", "start"]
