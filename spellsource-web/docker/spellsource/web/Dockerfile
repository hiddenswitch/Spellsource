FROM node:18-alpine3.17

COPY ./ /spellsource/

WORKDIR /spellsource/spellsource-web/
RUN apk add --no-cache --virtual .build-deps alpine-sdk python3 \
    && corepack enable \
    && yarn install \
    && yarn build \
    && apk del .build-deps \
    && yarn cache clean
  
EXPOSE 3000
ENTRYPOINT yarn start