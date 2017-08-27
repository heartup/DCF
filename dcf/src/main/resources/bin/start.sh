#!/bin/sh
export JAVA_BIN=$JAVA_HOME/bin

PRG="$0"
SERVER_HOME=`dirname "$PRG"`/..
SERVER_HOME=`cd "$SERVER_HOME" && pwd`

cd `dirname $0`
DEPLOY_DIR=`dirname $PWD`

echo "DEPLOY_DIR: $DEPLOY_DIR"

SERVER_NAME='DCF'

PIDS=`ps -ef | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $SERVER_NAME already started!"
    echo "PID: $PIDS"
    exit 1
fi

if [ -n "$SERVER_PORT" ]; then
    SERVER_PORT_COUNT=`netstat -tln | grep $SERVER_PORT | wc -l`
    if [ $SERVER_PORT_COUNT -gt 0 ]; then
        echo "ERROR: The $SERVER_NAME port $SERVER_PORT already used!"
        exit 1
    fi
fi

LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
JAVA_DEBUG_OPTS=""
if [ "$3" = "debug" ]; then
    JAVA_DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n "
fi
JAVA_JMX_OPTS=""
if [ "$3" = "jmx" ]; then
    JAVA_JMX_OPTS=" -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false "
fi
JAVA_MEM_OPTS=""
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
    JAVA_MEM_OPTS=" -server -Xmx300m -Xms200m -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
else
    JAVA_MEM_OPTS=" -server -Xmx300m -Xms200m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
fi

echo -e "Starting the $SERVER_NAME ..."
nohup java ${OPT} ${JAVA_OPTS} ${JAVA_MEM_OPTS} ${JAVA_DEBUG_OPTS} ${JAVA_JMX_OPTS} -Dfile.encoding=UTF-8 -Duser.country=CN -Ddcf.process=node -DSTOP.PORT=81003 -DSTOP.KEY=foo -Dserver.home="${SERVER_HOME}"  -cp "${LIB_JARS}":"${SERVER_HOME}"/etc io.reactivej.dcf.Main >/dev/null 2>&1 &
COUNT=0
while [ $COUNT -lt 1 ]; do
    sleep 1
    if [ -n "$SERVER_PORT" ]; then
		COUNT=`netstat -an | grep $SERVER_PORT | wc -l`
		echo "netstat check count[$COUNT]"
    else
    	COUNT=`ps -ef | grep java | grep "$DEPLOY_DIR" | awk '{print $2}' | wc -l`
    	echo "ps check count[$COUNT]"
    fi
    if [ $COUNT -gt 0 ]; then
        break
    fi
done

echo "OK!"
PIDS=`ps -ef | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
echo "PID: $PIDS"
echo "LOG DIR: logs"