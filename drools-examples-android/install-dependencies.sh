#!/bin/bash
#
# This script mavenizes all dependencies from the Android SDK required to build Robolectric.
#
# Usage:
#   install-dependencies.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs and Google APIs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install directory.
#  3. You have installed the Android Repository and Google Repository libraries from the SDK installer.

set -e

ANDROID_REPOSITORY=${ANDROID_HOME}/extras/android/m2repository
GOOGLE_REPOSITORY=${ANDROID_HOME}/extras/google/m2repository
TOOLS=${ANDROID_HOME}/build-tools

function install() {
  groupId=$1; artifactId=$2; version=$3; archive=$4
  pom="${archive%.*}.pom"
  extension=${archive##*.}

  if [ ! -f "$archive" ]; then
      echo "${groupId}:${artifactId} not found! Make sure that the 'Android Support Library' and 'Google Repository' is up to date in the SDK Manager."
      exit 1;
  fi
  echo "Installing ${groupId}:${artifactId} from ${archive} pomFile: ${pom}"
  if [ -f "$pom" ]; then
    mvn -q install:install-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=${extension} -Dfile="${archive}" -DpomFile="${pom}"
  else
    echo "${pom} not found! Generating"
    mvn -q install:install-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=${extension} -Dfile="${archive}"
  fi
}

function install_dx() {
  groupId=$1; artifactId=$2 version=$3;

  dir="${TOOLS}/${version}"

  if [ ! -d "$dir" ]; then
    echo "${groupId}:${artifactId} not found! Make sure that 'Build Tools' is up to date in the SDK manager for API ${version}."
    exit 1
  fi

  echo "Installing ${groupId}:${artifactId} from ${dir}/lib/dx.jar"
  mvn -q install:install-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=jar -Dfile="${dir}/lib/dx.jar"
}

if [ -z "${ANDROID_HOME}" ]; then
  echo "You must set \$ANDROID_HOME"
  exit 1
fi

# Install multidex
install "com.android.support" "multidex" "1.0.1" "${ANDROID_REPOSITORY}/com/android/support/multidex/1.0.1/multidex-1.0.1.aar"
install "com.android.support" "multidex-instrumentation" "1.0.1" "${ANDROID_REPOSITORY}/com/android/support/multidex-instrumentation/1.0.1/multidex-instrumentation-1.0.1.aar"
#install "com.android" "dx" "21.1.2" "${TOOLS}/21.1.2/lib/dx.jar"

# Install dx
install_dx "com.android" "dx" "21.1.2"
