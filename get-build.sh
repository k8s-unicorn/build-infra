#!/bin/bash

# Copyright 2015 The Kubernetes Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -o errexit
set -o nounset
set -o pipefail

KUBE_ROOT=$(dirname "${BASH_SOURCE}")/..
KUBE_RELEASE_VERSION_DASHED_REGEX="v(0|[1-9][0-9]*)-(0|[1-9][0-9]*)-(0|[1-9][0-9]*)(-([a-zA-Z0-9]+)-(0|[1-9][0-9]*))?"
KUBE_RELEASE_VERSION_REGEX="^v(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)(-([a-zA-Z0-9]+)\\.(0|[1-9][0-9]*))?$"

KUBE_CI_VERSION_REGEX="^v(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)-([a-zA-Z0-9]+)\\.(0|[1-9][0-9]*)(\\.(0|[1-9][0-9]*)\\+[-0-9a-z]*)?$"

declare -r KUBE_RELEASE_BUCKET_URL="https://storage.googleapis.com/kubernetes-release"
declare -r KUBE_DEV_RELEASE_BUCKET_URL="https://storage.googleapis.com/kubernetes-release-dev"
declare -r KUBE_TAR_NAME="kubernetes.tar.gz"

usage() {
  echo "${0} [-v] <version number or publication>"
  echo "  -v:  Don't get tars, just print the version number"
  echo ""
  echo '  Version number or publication is either a proper version number'
  echo '  (e.g. "v1.0.6", "v1.2.0-alpha.1.881+376438b69c7612") or a version'
  echo '  publication of the form <bucket>/<version> (e.g. "release/stable",'
  echo '  "ci/latest-1").  Some common ones are:'
  echo '    - "release/stable"'
  echo '    - "release/latest"'
  echo '    - "ci/latest"'
  echo '  See the docs on getting builds for more information about version'
  echo '  publication.'
}

# Sets KUBE_VERSION variable to the proper version number (e.g. "v1.0.6",
# "v1.2.0-alpha.1.881+376438b69c7612") or a version' publication of the form
# <path>/<version> (e.g. "release/stable",' "ci/latest-1").
#
# See the docs on getting builds for more information about version
# publication.
#
# Args:
#   $1 version string from command line
# Vars set:
#   KUBE_VERSION
function set_binary_version() {
  if [[ "${1}" =~ "/" ]]; then
    IFS='/' read -a path <<< "${1}"
    if [[ "${path[0]}" == "release" ]]; then
      KUBE_VERSION=$(gsutil cat "gs://kubernetes-release/${1}.txt")
    else
      KUBE_VERSION=$(gsutil cat "gs://kubernetes-release-dev/${1}.txt")
    fi
  else
    KUBE_VERSION=${1}
  fi
}

print_version=false

while getopts ":vh" opt; do
  case ${opt} in
    v)
      print_version="true"
      ;;
    h)
      usage
      exit 0
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      usage
      exit 1
      ;;
  esac
done
shift $((OPTIND-1))

if [[ $# -ne 1 ]]; then
    usage
    exit 1
fi

set_binary_version "${1}"

if [[ "${print_version}" == "true" ]]; then
  echo "${KUBE_VERSION}"
else
  echo "Using version at ${1}: ${KUBE_VERSION}" >&2
  if [[ ${KUBE_VERSION} =~ ${KUBE_RELEASE_VERSION_REGEX} ]]; then
    curl --fail -o "kubernetes-${KUBE_VERSION}.tar.gz" "${KUBE_RELEASE_BUCKET_URL}/release/${KUBE_VERSION}/${KUBE_TAR_NAME}"
  elif [[ ${KUBE_VERSION} =~ ${KUBE_CI_VERSION_REGEX} ]]; then
    curl --fail -o "kubernetes-${KUBE_VERSION}.tar.gz" "${KUBE_DEV_RELEASE_BUCKET_URL}/ci/${KUBE_VERSION}/${KUBE_TAR_NAME}"
  else
    echo "Version doesn't match regexp" >&2
    exit 1
  fi
fi
