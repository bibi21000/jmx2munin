# munin-plugin-jmx2munin


The [munin-plugin-jmx2munin](http://github.com/bibi21000/munin-plugin-jmx2munin) is a fork of the [jmx2munin](http://github.com/tcurdt/jmx2munin) project. jmx2munin exposes JMX MBean attributes to [Munin](http://munin-monitoring.org/).

This fork also includes :

 * debian files for build packages
 * a collection of cassandra (2.0) configurations files

# How to use

 * clone this repo
 * build the debian package :

    dpkg-builbpackage

 * install the generated packages :

    dpkg -i *.gen

 * edit munin-node configuration :

    sudoedit /etc/munin/plugin-conf.d/munin-node

 * add the following lines

    [cassandra_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query org.apache.cassandra.*:*

    [cassandra_jvm_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query java.lang:*

 * go to /usr/share/munin/plugins/jmx2munin.cfg/cassandra

    cd /usr/share/munin/plugins/jmx2munin.cfg/cassandra

 * install the default scripts :

    sudo ./install.sh

 * you can also get informations for a specific column family. Generate the conf with the followinf command :

    sudo ./generate_cf_cfg.sh keyspace cf_name

 * and re-run the install script :

    sudo ./install.sh

 * restart munin-node :

    sudo /etc/init.d/munin-node restart

 * and wait ... Data should appears on your master after a while.


# How to extend

The jmx2munin.jar (located in /usr/share/munin) could help you to discover JMX metrics.

Use the following command to get all metrics for the vassandra database :

    java -jar jmx2munin.jar \
         -url service:jmx:rmi:///jndi/rmi://localhost:7199/jmxrmi \
         -query "org.apache.cassandra.*:*"

This one gives informations about the virutal machine :

    java -jar jmx2munin.jar \
         -url service:jmx:rmi:///jndi/rmi://localhost:7199/jmxrmi \
         -query "java.lang:*"

Put your configuration files in /usr/share/munin/plugins/jmx2munin.cfg.
You must respect the <directory>/script structures. For example, if you want to monitor the number of applications in your tomcat server
think about someting tomcat7/appwars.

When creating links in /etc/munin/plugins, also respect this rule. It's the way the script retrieve its configuration. For the
preceding example :

    ln -s /usr/share/munin/plugins/jmx2munin /etc/munin/plugins/tomcat7_appwars

Finally add a configuration section in /etc/munin/plugin-conf.d/munin-node :

     [tomcat7_*]
     env.url .....
     env.query .....

At last, send a push request and I'll be packaging your configartion files ;)

For more informations, look at cassandra examples and [jmx2munin](http://github.com/tcurdt/jmx2munin).

# License

Licensed under the Apache License, Version 2.0 (the "License")
You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0/>

Contains also code under GPL-3.0+
You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
