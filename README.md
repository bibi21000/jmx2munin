# munin-plugin-jmx2munin


The [munin-plugin-jmx2munin](http://github.com/bibi21000/munin-plugin-jmx2munin) is a fork of the [jmx2munin](http://github.com/tcurdt/jmx2munin) project. jmx2munin exposes JMX MBean attributes to [Munin](http://munin-monitoring.org/).

This fork also includes :

 * a low level cache for JMX connections (reduce CPU use 10x)
 * authentication for JMX
 * fully compliant with --suggest option
 * debian files for building packages for Ubuntu/Debian
 * scripts and configurations files for cassandra (2.0)
 * scripts and configurations files for tomcat (6.0)

# Installation

Clone this repo

Build the debian package :

    dpkg-builbpackage

Resolve dependencies and built it again :) :

    dpkg-builbpackage

Install the generated packages :

    dpkg -i *.deb

Or build the RPM, after installing dependencies :

	debbuild -ba -vv munin-plugin-jmx2munin.spec
	
You can find prebuilt packages for Debian/ ubuntu [here] (http://bibi21000.gallet.info/index.php/fr/ubuntu-fr/127-depots-ubuntu/administrateurs-systemes/212-ajouter-le-depot-pour-les-administrateurs-systemes.html)
and for Fedora [here] (http://bibi21000.gallet.info/index.php/fr/ubuntu-fr/257-des-depots-fedora.html)

# Cassandra

Go to /usr/share/munin/plugins/jmx2munin.cfg/cassandra2

    cd /usr/share/munin/plugins/jmx2munin.cfg/cassandra2

Install the default scripts :

    sudo ./install.sh

You can also get informations for a specific column family. Generate the conf with the followinf command :

    sudo ./generate_cf_cfg.sh keyspace cf_name

and re-run the install script :

    sudo ./install.sh

Update configuration in /etc/munin/plugin-conf.d/cassandra2

    [cassandra2_db_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query org.apache.cassandra.db:*
    env.ttl 60
    [cassandra2_int_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query org.apache.cassandra.internal:*
    env.ttl 60
    [cassandra2_jvm_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query java.lang:*
    env.ttl 60
    [cassandra2_met_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query org.apache.cassandra.metrics:*
    env.ttl 60
    [cassandra2_req_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query org.apache.cassandra.request:*
    env.ttl 60

You can also use a more generic configuration :

    [cassandra2_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query org.apache.cassandra.*:*
    env.ttl 60
    [cassandra2_jvm_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:7199/jmxrmi
    env.query java.lang:*
    env.ttl 60

But keep in mind that in this case, the full JMX datas (more than 3MB) will be requested at a time. In the first example, all data will be downloaded but in 4 parts.

Restart munin-node :

    sudo /etc/init.d/munin-node restart

and wait ... and wait ... and wait ... Data should appears on your master after a while (15 minutes).

# Tomcat

Go to /usr/share/munin/plugins/jmx2munin.cfg/tomcat6

    cd /usr/share/munin/plugins/jmx2munin.cfg/tomcat6

Install the default scripts :

    sudo ./install.sh

Update configuration in /etc/munin/plugin-conf.d/tomcat6

    [tomcat6_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:9012/jmxrmi
    env.query Catalina:*
    env.ttl 60

    [tomcat6_jvm_*]
    env.url service:jmx:rmi:///jndi/rmi://127.0.0.1:9012/jmxrmi
    env.query java.lang:*
    env.ttl 60

Restart munin-node :

    sudo /etc/init.d/munin-node restart

and wait ... and wait ... and wait ... Data should appears on your master after a while (15 minutes).

# Performances

Collecting data with JMX is a heavy task.
On my small test cluster (1 core/1.5G RAM), gathering data for about 10 jmx2munin plugins takes approximatively 50% of CPU !!!
This plugin retrieve all data available by JMX each time the plugin is called. I decided to implement a low level cache.

So, it's recomended to cache all calls to JMX (using the ttl env config).

You can get the duration of the JMX request using "-debug 1" as parameter for jmx2munin.jar. You can get the cache size looking at the /tmp directory.

If you need authentication, add the following line to your configuration file :

    env.username remote_user
    env.password remote_password

For paranoids, add :

    env.cryptkey mylooooongkey

This key will be use to crypt data in the cache local store.

# How to extend

The jmx2munin.jar (located in /usr/share/munin) could help you to discover JMX metrics.

Use the following command to get all metrics for the cassandra database :

    java -jar jmx2munin.jar \
         -url service:jmx:rmi:///jndi/rmi://localhost:7199/jmxrmi \
         -query "org.apache.cassandra.*:*"

This one gives informations about the virutal machine :

    java -jar jmx2munin.jar \
         -url service:jmx:rmi:///jndi/rmi://localhost:7199/jmxrmi \
         -query "java.lang:*"

Put your configuration files in /usr/share/munin/plugins/jmx2munin.cfg.
You must respect the <directory>/script structures. For example, if you want to monitor the number of applications in your tomcat server
think about something tomcat7/appwars.

When creating links in /etc/munin/plugins, also respect this rule. It's the way the script retrieve its configuration. For the
preceeding example :

    ln -s /usr/share/munin/plugins/jmx2munin /etc/munin/plugins/tomcat7_appwars

Finally add a configuration section in /etc/munin/plugin-conf.d/munin-node :

     [tomcat7_*]
     env.url .....
     env.query .....

It's important to respect this rules otherwise some functiunalities (like --suggest) will not work.

At last, send a push request :) Patches are also accepted :D

For more informations, look at included examples and [jmx2munin](http://github.com/tcurdt/jmx2munin).

# License

Licensed under the Apache License, Version 2.0 (the "License")
You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0/>

Contains also code under GPL-3.0+
You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
