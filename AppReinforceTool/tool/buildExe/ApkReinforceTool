#!/usr/bin/env bash

DIRNAME=$(dirname "$0")

nohup java -jar -splash:${DIRNAME}/icon/app_icon.png  ${DIRNAME}/libs/ApkReinforceTool.jar "$@" >/dev/null &

exit $?
