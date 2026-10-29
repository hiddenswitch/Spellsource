const path=require('node:path').resolve(__dirname, '../..');
if (!process.env.SCHEMA_TEST_GRAPHQL_URL) throw new Error('Set SCHEMA_TEST_GRAPHQL_URL to the isolated GraphQL server');
const { getIntrospectionQuery, buildClientSchema, validate }=require(path+'/node_modules/graphql');
require(path+'/node_modules/ts-node').register({transpileOnly:true,compilerOptions:{module:'CommonJS',moduleResolution:'node',target:'ES2022'}});
(async()=>{
 const response=await fetch(process.env.SCHEMA_TEST_GRAPHQL_URL,{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({query:getIntrospectionQuery()})});
 const result=await response.json();if(result.errors)throw Error('Schema introspection failed');
 const schema=buildClientSchema(result.data); const errors=[];let count=0;
 for(const workspace of ['spellsource-web','spellsource-threejs']){
  const exports=require(path+'/'+workspace+'/src/__generated__/client.ts');
  for(const [name,doc] of Object.entries(exports)){
   if(!name.endsWith('Document')||doc?.kind!=='Document')continue;
   count++;for(const e of validate(schema,doc))errors.push({workspace,operation:name,message:e.message});
  }
 }
 const summary={operations:count,errors};console.log(JSON.stringify(summary));if(errors.length)process.exitCode=1;
})().catch(e=>{console.error(e.message);process.exitCode=1});
