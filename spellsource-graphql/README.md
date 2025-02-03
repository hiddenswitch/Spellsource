# Spellsource GraphQL

This package has the Spellsource Postgraphile server, and it also stores and handles the codegen for the .graphql operations for the website/client/server

## Postgraphile Server

Now decoupled from the website

`yarn develop` or `gradle yarnRunDevelop`

## GraphQL Operations

GraphQL operations are defined in .graphql files within the `graphql` folder

`graphql/web/**` will be generated in typescript for the Website `spellsource-web`

`graphql/client/**` will be generated in C# for the Unity Client `spellsource-client`

`graphql/server/**` will be generated in Java for the backend server `spellsource-server`

`graphql/shared/**` will be generated for all

## GraphQL Code Generation

To generate all typescript, C#, and Java code and live update them:
`gradle generateAll --continuous`

### TypeScript

Generates using [GraphQL Codegen](https://the-guild.dev/graphql/codegen) configured in `codegen.yaml`

`yarn codegen`

### C#

Generates using [Strawberry Shake](https://chillicream.com/docs/strawberryshake) configured in `.graphqlrc.json`

`yarn starwberry-shake`

### Java
Generates using [GraphQL Java Codegen](https://kobylynskyi.github.io/graphql-java-codegen/) configured in `build.gradle`

`gradle graphqlCodegen`