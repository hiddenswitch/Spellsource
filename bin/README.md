# Binary development utilities

To better support Windows wherever possible, Spellsource vendors:

 - `busybox` to support `bash` scripts on Windows from inside Gradle.
 - Wrappers using `docker` for `dotnet`, `grpc_csharp_plugin` and others.
 - The cluster management binaries `helm` and `kubectl`.
 - `pg_dump` to support the JOOQ code generation workflow.
 - `gsudo` as `sudo.exe` for Windows privilege escalation.