#!/bin/sh

newVersion=`perl -npe "s/version in ThisBuild\s+:=\s+\"(.*)\"/\1/" version.sbt | sed -e "/^$/d"`

for f in $(/bin/ls src/sbt-test/sbt-one-log/*/project/plugins.sbt); do
  echo $f;
  perl -i -npe "s/addSbtPlugin\(\"com.zavakid.sbt\".*/addSbtPlugin\(\"com.zavakid.sbt\" % \"sbt-one-log\" % \"$newVersion\"\)/" $f; \
done;
