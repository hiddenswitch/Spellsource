#!/usr/bin/env bash
export PORT=8080
# Put a URL override for the client
cat << EOF > ../Minionate/Assets/Resources/Url.txt
http://localhost:$PORT
EOF
./gradlew net:run --continuous
rm ../Minionate/Assets/Resources/Url.txt
rm ../Minionate/Assets/Resources/Url.txt.neta
