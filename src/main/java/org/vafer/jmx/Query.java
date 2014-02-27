package org.vafer.jmx;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.System;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.vafer.jmx.munin.Version;

public final class Query {


    public void run(String url, Map credentials, String expression, Filter filter, Output output, int ttl, int debug, String cryptkey) throws IOException, MalformedObjectNameException, InstanceNotFoundException, ReflectionException, IntrospectionException, AttributeNotFoundException, MBeanException {
        JMXConnector connector = null;
        long start_at = System.currentTimeMillis();
        try {
            CacheOutput cache = null;
            boolean timeout = true;
            if (ttl!=0) {
                //Retrieve the serial file and evaluate timeout
                cache = new CacheOutput(url,expression, cryptkey, debug);
                if (cache.timeout(ttl)==false) {
                    //System.err.println("set timeout to false");
                    timeout=false;
                }
            }
            if ( (timeout==false) && (ttl!=0)) {
                if (cache.readFromFile()==false) {
                    timeout=true;
                }
            }
            if (timeout) {
                Collection<ObjectInstance> mbeans = null;
                if (credentials==null) {
                    connector = JMXConnectorFactory.connect(new JMXServiceURL(url));
                } else {
                    connector = JMXConnectorFactory.connect(new JMXServiceURL(url),credentials);
                }
                MBeanServerConnection connection = connector.getMBeanServerConnection();
                //We should talk to the client via jmx
                mbeans = connection.queryMBeans(new ObjectName(expression), null);
                //System.err.println("Data retrieved from server " + url);
                for(ObjectInstance mbean : mbeans) {
                    final ObjectName mbeanName = mbean.getObjectName();
                    final MBeanInfo mbeanInfo = connection.getMBeanInfo(mbeanName);
                    final MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
                    for (final MBeanAttributeInfo attribute : attributes) {
                        if (attribute.isReadable()) {
                            if (filter.include(mbeanName, attribute.getName())) {
                                final String attributeName = attribute.getName();
                                try {
                                    if ((ttl!=0)) {
                                        ObjectName objNane=mbean.getObjectName();
                                        Object value = connection.getAttribute(mbeanName, attributeName);
                                        cache.output(
                                                objNane,
                                                attributeName,
                                                value
                                                );
                                        output.output(
                                                objNane,
                                                attributeName,
                                                value
                                                );
                                    } else {
                                        output.output(
                                                mbean.getObjectName(),
                                                attributeName,
                                                connection.getAttribute(mbeanName, attributeName)
                                                );
                                    }
                                } catch(Exception e) {
                                    if (debug>1) {
                                        System.err.println("Failed to read " + mbeanName + "." + attributeName);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (debug>3) {
                    System.err.println("Get data from cache");
                }
                //We use data stored in cache
                Enumeration enumeration = cache.store.elements();
                while (enumeration.hasMoreElements()) {
                    CacheItem item = (CacheItem)enumeration.nextElement();
                    output.output(
                            item.beanName,
                            item.attributeName,
                            item.value
                            );
                }
            }
            if ((ttl!=0) && timeout) {
                cache.storeInFile();
            }

        } finally {
            if (connector != null) {
                connector.close();
            }
            if (debug>0) {
                System.err.println("JMX request duration (ms) : " + (System.currentTimeMillis()-start_at));
                System.err.println("jmx2munin version " + Version.VERSION);
            }
        }
    }
}
