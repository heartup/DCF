#! /bin/sh
while true
do
	sleep 3600
	process_id=`netstat -atnp | grep ${exitport} | gawk 'BEGIN {FS = "[ |/]+";} /${exitport}/ {print $7;}'`
	if [ -n "$process_id" ];then
		cd ${home.path}/bin
		./stop.sh && ./run.sh
	fi
done
