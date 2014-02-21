package org.vafer.jmx;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable ;

import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;

import java.lang.System;

import javax.management.ObjectName;

public final class CacheItem implements Serializable {
/* data items in cache
 *
 */
    public ObjectName beanName = null;
    public String attributeName = null;
    public Object value = null;

    public CacheItem(ObjectName mbeanName, String mattributeName, Object mvalue) {
        beanName = mbeanName;
        attributeName =mattributeName;
        value = mvalue;
       }

}
