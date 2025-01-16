FROM node:22-alpine

COPY ./ /spellsource/

WORKDIR /spellsource/spellsource-graphql/
RUN apk add --no-cache --virtual .build-deps alpine-sdk \
    && corepack enable \
    && yarn install \
    && yarn build \
    && apk del .build-deps \
    && yarn cache clean
  
EXPOSE 3000
ENTRYPOINT yarn start