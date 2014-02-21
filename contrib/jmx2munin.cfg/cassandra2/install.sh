#!/bin/bash

# Creates symlinks in /etc/munin/plugins for all the conf files except the
# standard1 ones

link_target=$(readlink -f ../../jmx2munin)

for file in *; do
    [[ "$file" != *standard1* ]] || continue
    [[ "$file" != *\.sh* ]] || continue
    [[ "$file" != *\.conf* ]] || continue
    link="/etc/munin/plugins/cassandra2_$file"
    [ -f $link ] && echo "$link already exists. Remove it" && rm -f $link
    echo "$link -> $link_target"
    ln -s "$link_target" "$link"
done
[ ! -f /etc/munin/plugin-conf.d/cassandra2 ] && cp -a cassandra2.conf /etc/munin/plugin-conf.d/cassandra2

