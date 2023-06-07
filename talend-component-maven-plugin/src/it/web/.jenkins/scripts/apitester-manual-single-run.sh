#!/usr/bin/env bash
#
#  Copyright (C) 2006-2023 Talend Inc. - www.talend.com
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

set -xe

function usage(){
  printf 'Manually run an apitester campaign on a single test project with default parameters\n'
  printf 'Usage : %s <account_id>' "${0}"
  printf '\n'
  printf '%s\n' "${1}"
  printf '\n'
  exit 1
}

# Parameters:
[ -z ${1+x} ] && usage 'Parameter "account_id" is needed.'
[ -z ${1+x} ] && usage 'Parameter "sub_test" is needed.'

ACCOUNT_ID=${1}
FILE_TO_RUN=${2}

path=$(dirname -- "$( readlink -f -- "$0"; )";)

main() (
  printf '##############################################\n'
  printf 'Api Tester run\n'
  printf 'from %s\n' "${path}"
  printf '##############################################\n'

  test_run
)

# CI account id is stored in 31e17fe5-9718-4a80-a8b2-593c73a5bcfc at
# https://vault-vaas.service.cd.datapwn.com/ui/vault/secrets/secret/show/component/jenkins-connectors


function test_run {

  cd "${path}/../../test"

  mvn clean test --file="./$FILE_TO_RUN/pom.xml" \
                 --settings="${path}/../settings.xml" \
                 --define instance="eu" \
                 --define accountId="${ACCOUNT_ID}" \
                 --define selectedEnvironment="component_runtime_ci" \
                 --define stopOnFailure=false
}

main "$@"