Summary:            The jmx2munin project exposes JMX MBean attributes to Munin.
Name:               munin-plugin-jmx2munin
Version:            2.0
Release:            30%{?dist}
License:            GPLv3
Group:              System Environment/Daemons
Source:             %{name}-%{version}.tar.gz
URL:                https://github.com/bibi21000/munin-plugin-jmx2munin
Requires:      		java-openjdk
Requires:      		munin-node
BuildRequires:      maven
BuildRequires:      java-1.7.0-openjdk-devel
BuildRequires:      maven-surefire-plugin
BuildRequires:      maven-surefire-provider-junit
BuildRequires:      beust-jcommander
BuildArch: 			noarch

%description
Some of it's features:
 - strictly complies to the plugin format
 - exposes composite types like Lists, Maps, Set as useful as possible
 - String values can be mapped to numbers

%package cassandra
Group: System Environment/Daemons
Summary: Cassandra configurations for jmx2munin
Requires: munin-plugin-jmx2munin = %{version}-%{release}

%description cassandra
Monitor jvm, cluster, column families, ...

%package tomcat
Group: System Environment/Daemons
Summary: Tomcat configurations for jmx2munin
Requires: munin-plugin-jmx2munin = %{version}-%{release}

%description tomcat
Monitor jvm, catalina, ...

%prep
%autosetup -n %{name}

%build
mvn compile

%clean
rm -rf %{buildroot}

%install
mvn install
mkdir -p %{buildroot}/usr/share/munin
mkdir -p %{buildroot}/usr/share/munin/plugins
mkdir -p %{buildroot}/%{_sysconfdir}/munin/plugin-conf.d/
install -m 755 target/jmx2munin-2.0.jar %{buildroot}/usr/share/munin/

cp -Rf contrib/jmx2munin.cfg %{buildroot}/usr/share/munin/plugins

cp contrib/jmx2munin.cfg/cassandra2/cassandra2.conf %{buildroot}/%{_sysconfdir}/munin/plugin-conf.d/cassandra2
chmod 640 %{buildroot}/%{_sysconfdir}/munin/plugin-conf.d/cassandra2
chmod 644 %{buildroot}/usr/share/munin/plugins/jmx2munin.cfg/cassandra2/*
chmod 640 %{buildroot}/usr/share/munin/plugins/jmx2munin.cfg/cassandra2/*.conf
chmod 755 %{buildroot}/usr/share/munin/plugins/jmx2munin.cfg/cassandra2/*.sh
ln -s /usr/share/munin/plugins/jmx2munin %{buildroot}/usr/share/munin/plugins/cassandra2_

cp contrib/jmx2munin.cfg/tomcat6/tomcat6.conf %{buildroot}/%{_sysconfdir}/munin/plugin-conf.d/tomcat6
chmod 640 %{buildroot}/%{_sysconfdir}/munin/plugin-conf.d/tomcat6
chmod 644 %{buildroot}/usr/share/munin/plugins/jmx2munin.cfg/tomcat6/*
chmod 640 %{buildroot}/usr/share/munin/plugins/jmx2munin.cfg/tomcat6/*.conf
chmod 755 %{buildroot}/usr/share/munin/plugins/jmx2munin.cfg/tomcat6/*.sh
ln -s /usr/share/munin/plugins/jmx2munin %{buildroot}/usr/share/munin/plugins/tomcat6_

ln -s /usr/share/munin/jmx2munin-2.0.jar %{buildroot}/usr/share/munin/jmx2munin.jar
install -m 755 contrib/jmx2munin.sh %{buildroot}/usr/share/munin/plugins/jmx2munin

%files cassandra
%defattr(-, root, root)
/usr/share/munin/plugins/jmx2munin.cfg/cassandra2/*
/usr/share/munin/plugins/cassandra2_
%config(noreplace) %{_sysconfdir}/munin/plugin-conf.d/cassandra2

%files tomcat
%defattr(-, root, root)
/usr/share/munin/plugins/tomcat6_
/usr/share/munin/plugins/jmx2munin.cfg/tomcat6/*
%config(noreplace) %{_sysconfdir}/munin/plugin-conf.d/tomcat6

%files
%doc README.md
%dir /usr/share/munin/plugins/jmx2munin.cfg
/usr/share/munin/plugins/jmx2munin
/usr/share/munin/*.jar

%changelog
* Wed Apr 9 2014 Sébastien GALLET <sgallet@gmail.com> - 2.0-30
- Add mode scripts for cassandra
- Add spec for Redhat/Fedora users
- Update doc

* Fri Mar 7 2014 Sébastien GALLET <sgallet@gmail.com> - 2.0-29
- Initial package
