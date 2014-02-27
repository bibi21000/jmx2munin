#!/bin/bash
# -*- sh -*-

: << =cut

=head1 NAME

JMX plugin for Cassandra, Tomcat, ...

=head1 CONFIGURATION

[cassandra2_*]
env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
env.query org.apache.cassandra.*:*

[tomcat6_*]
env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:9012/jmxrmi
env.query Catalina:*

=head1 AUTHOR

bibi21000 <bibi21000@gmail.com>

=head1 LICENSE

APLv2
GPLv2

=head1 MAGICK MARKERS

 #%# family=auto
 #%# capabilities=autoconf suggest

=cut

if [ -z "$MUNIN_LIBDIR" ]; then
    MUNIN_LIBDIR="`dirname $(dirname "$0")`"
fi

if [ -f "$MUNIN_LIBDIR/plugins/plugin.sh" ]; then
    . $MUNIN_LIBDIR/plugins/plugin.sh
fi

if [ "$1" == "autoconf" ]; then
    echo yes
    exit 0
fi

if [ "$1" = "suggest" ]; then
    plugin=$(echo ${0} | sed -e "s#.*/##g" -e "s/_*$//g" )
    [ ! -z "$MUNIN_LIBDIR" ] && [ -d "$MUNIN_LIBDIR"/plugins ] && cd "$MUNIN_LIBDIR"/plugins
    [ ! -d jmx2munin.cfg/${plugin} ] && exit 0
    cd jmx2munin.cfg/${plugin}/
    for file in *; do
        [[ "$file" != *standard1* ]] || continue
        [[ "$file" != *\.sh* ]] || continue
        [[ "$file" != *\.conf* ]] || continue
        echo "$file"
    done
    exit 0
fi

if [ -z "$url" ]; then
  # this is very common so make it a default
  url="service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi"
fi

if [ ! -z "$ttl" ]; then
  # a time to live is set. We will use the cache
  CACHEOPTS="-ttl $ttl"
else
  CACHEOPTS=""
fi

if [ ! -z "$cryptkey" ]; then
  # a cryptkey is available. We will use it
  CACHEOPTS="-cryptkey $cryptkey" CACHEOPTS
fi

[ -z "$config" ] && config=$(echo ${0} | sed -e "s#.*/##g" -e "s#_#/#")

if [ -z "$config" -o -z "$query" -o -z "$url" ]; then
  echo "Configuration needs attributes config, query and optinally url"
  [ $config = "jmx2munin" ] && exit 0
  exit 1
fi

JMX2MUNIN_DIR="$MUNIN_LIBDIR/plugins"
CONFIG="$JMX2MUNIN_DIR/jmx2munin.cfg/$config"

if [ "$1" = "config" ]; then
    cat "$CONFIG"
    exit 0
fi

JAR="$MUNIN_LIBDIR/jmx2munin.jar"
CACHED="${MUNIN_STATEFILE}"

if test ! -f $CACHED || test `find "$CACHED" -mmin +2`; then

    java -jar "$JAR" \
      -url "$url" \
      -query "$query" \
      $CACHEOPTS \
      $ATTRIBUTES \
      > $CACHED

    echo "cached.value `date +%s`" >> $CACHED
fi

ATTRIBUTES=`awk '/\.label/ { gsub(/\.label/,""); print $1 }' $CONFIG`

if [ -z "$ATTRIBUTES" ]; then
  echo "Could not find any *.label lines in $CONFIG"
  exit 1
fi

for ATTRIBUTE in $ATTRIBUTES; do
  grep "$ATTRIBUTE\." $CACHED
done

exit 0
