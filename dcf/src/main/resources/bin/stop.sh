#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR1=`pwd`
DEPLOY_DIR2=`pwd -P`
SERVER_NAME="DCF"

NODE_KEYWORD="$DEPLOY_DIR1"
PIDS1=`ps -ef | grep java| grep $NODE_KEYWORD |awk '{print $2}'`

NODE_KEYWORD="$DEPLOY_DIR2"
PIDS2=`ps -ef | grep java| grep $NODE_KEYWORD |awk '{print $2}'`

PIDS="$PIDS1 $PIDS2"

if [ -z "$PIDS" ]; then
    echo "ERROR: The $SERVER_NAME does not started!"
    exit 1
fi
for PID in $PIDS ; do
    kill -9 $PID > /dev/null 2>&1
done



COUNT=0
while [ $COUNT -lt 1 ]; do
    echo -e ".\c"
    sleep 1
    COUNT=1
    for PID in $PIDS ; do
        PID_EXIST=`ps -f -p $PID | grep java`
        if [ -n "$PID_EXIST" ]; then
            COUNT=0
            break
        fi
    done
done

echo "OK!"
echo "PID: $PIDS"

