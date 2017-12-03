#!/usr/bin/env bash
set -e
./gradlew swagger

OUTPUT_DIR=$(pwd)/../Minionate/Assets/Plugins/Client

swagger-codegen generate -DsupportingFiles=false -DapiTests=false -DmodelTests=false -DmodelDocs=false -DapiDocs=false -o $OUTPUT_DIR -c "csharpconfig.json"  -i "swagger-api.yaml" -l csharp
rm -rf $OUTPUT_DIR/src/
rm -rf $OUTPUT_DIR/docs/
rm -f $OUTPUT_DIR/build.bat
rm -f $OUTPUT_DIR/build.sh
rm -f $OUTPUT_DIR/git_push.sh
rm -f $OUTPUT_DIR/Spellsource.Client.sln
rm -f $OUTPUT_DIR/mono_nunit_test.sh
rm -f $OUTPUT_DIR/README.md
rm -rf $OUTPUT_DIR/Scripts/Spellsource.Client/Properties
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/packages.config
