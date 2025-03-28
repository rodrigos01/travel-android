#!/bin/bash

CONFIG_PATH=/config
WORKSPACE_PATH=/workspace

build_number=1
if [ -s "${CONFIG_PATH}/buildnum" ]; then
    echo "current build number: $(cat "${CONFIG_PATH}/buildnum")"
    build_number=$(awk '{$1=1+$1;print}' < "${CONFIG_PATH}/buildnum")
fi
echo "new build number: ${build_number}"
echo "${build_number}" > "${CONFIG_PATH}/buildnum"
echo "BUILD_NUMBER=${build_number}" > "${WORKSPACE_PATH}/.env"
