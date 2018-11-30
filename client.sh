#!/usr/bin/env bash
set -e
mkdir -pv "clientcsharp"
rm -rf "./client/"
gradle swagger
gradle swaggerClient

INPUT_DIR="clientcsharp"
OUTPUT_DIR="../Spellsource-Client/Assets/Plugins/Client"
rm -rf $OUTPUT_DIR
mv $INPUT_DIR $OUTPUT_DIR
rm -rf $INPUT_DIR
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
rm -rf $OUTPUT_DIR/Scripts/Spellsource.Client/Api
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/ApiClient.cs
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/ApiException.cs
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/ApiResponse.cs
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/Configuration.cs
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/ExceptionFactory.cs
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/GlobalConfiguration.cs
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/IApiAccessor.cs
rm -f $OUTPUT_DIR/Scripts/Spellsource.Client/Client/IReadableConfiguration.cs
