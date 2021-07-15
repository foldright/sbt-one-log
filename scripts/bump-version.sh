#!/bin/bash
set -eEuo pipefail

# change work dir to project root
cd "$(dirname "$(readlink -f "$0")")"/.. || {
  echo "Fail to change work dir!" 1>&2
  exit 1
}

if [ $# != 1 ]; then
  {
    echo "Only 1 argument for version!"
    echo
    echo "usage: $0 <new version>"
  } 1>&2

  exit 1
fi

readonly version="$1"

# update version for version.sbt file

readonly version_sbt_file=version.sbt

echo "update $version_sbt_file to version $version"
sed -ri 's/(version\s+in\s+ThisBuild\s+:=\s+)(".*)(".*)/\1"'"$version"'\3/' "$version_sbt_file"


# update version for plugins.sbt files

for f in src/sbt-test/sbt-one-log/*/project/plugins.sbt; do
  echo "update $f to version $version"

  sed -ri 's/addSbtPlugin\s*\("com.zavakid.sbt"\s*%\s*"(.*)"\s*%\s*"(.*)(".*)/addSbtPlugin\("com.zavakid.sbt" % "\1" % "'"$version"'\3/' "$f"
done
