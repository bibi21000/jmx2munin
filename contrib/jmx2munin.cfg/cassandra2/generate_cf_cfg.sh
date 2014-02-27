#!/bin/bash
#
# This file was pretty much lifted from:
# https://github.com/jancona/cassandra-munin-plugins/blob/master/tools/generate_cf_conf.sh
#
# Use the cassandra_standard1_* files as templates to build configurations
# for new keyspaces and column families we wish to monitor
#
# Author: Jim Ancona <jim@anconafamily.com>
#   Date: 17-Dec-2010
#
# Updated by : bibi21000 <bibi21000@gmail.com>
#   Date: 18-Feb-2014
#
# Exit here if we don't have two parameters
if [ $# -ne 2 ]; then
    echo "Usage: $0 <keyspace> <column_family>" >&2
    exit 1
fi
#
KEYSPACE=$(echo ${1} | sed 's/.*/\L&/')
COLUMNFAMILY=$(echo ${2} | sed 's/.*/\L&/')
#
for filename in standard1_*;
do
    outfile=$(echo $filename | sed "s/standard1/db_${KEYSPACE}_${COLUMNFAMILY}/")
    echo $outfile
    sed -e "s/Keyspace1/$KEYSPACE/g" -e "s/Standard1/$COLUMNFAMILY/g" < $filename > $outfile
    chmod 644 $outfile
done
exit 0
