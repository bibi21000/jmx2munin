package org.vafer.jmx;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;

import java.lang.System;

import javax.management.ObjectName;

import org.vafer.jmx.munin.MuninOutput;

public final class CacheOutput implements Output {
/* output data to cache
 *
 */
    public Hashtable store = new Hashtable();
    private String filename;

    public CacheOutput(String url, String expression) {
        File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));
        File fileInDirectory = new File(tmpDirectory, sanitizeFilename(url,expression));
        filename = fileInDirectory.getPath();
       }

    public static String sanitizeFilename(String url,String expression) {
        String motif = "[.{}:\\\\/*?|<>]";
        return "jmx2munin##".concat(url.replaceAll(motif, "_")).concat("##").concat(expression.replaceAll(motif, "_"));
    }

    public boolean timeout(int ttl) {
        File fileInDirectory = new File(filename);
        //System.err.println("file name " + filename);
        //System.err.println("file timeout " + (fileInDirectory.lastModified()+(ttl*1000)));
        //System.err.println("systime      " + System.currentTimeMillis());
        if (fileInDirectory.lastModified() == 0) {
            //System.err.println("set timeout to false");
            return true;
        }
        if ((fileInDirectory.lastModified()+ttl*1000)>System.currentTimeMillis()) {
            //System.err.println("set timeout to false");
            return false;
        } else {
            return true;
        }
    }

    public boolean readFromFile() {
        //Try to restore cache from disk
        FileInputStream fis = null;
        ObjectInputStream fin = null;
        try{
            fis = new FileInputStream(filename);
            fin = new ObjectInputStream(fis);
            store = (Hashtable)fin.readObject();
            fin.close();
            return true;
        } catch (IOException ex){
            //ex.printStackTrace();
            deleteFile();
            return false;
        } catch (ClassNotFoundException cnfe){
            //cnfe.printStackTrace();
            deleteFile();
            return false;
        }
    }

    public boolean deleteFile() {
        try{
            File file = new File(filename);
            if(file.delete()){
                return false;
            }else{
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean storeInFile() {
        //Try to restore cache from disk
        FileOutputStream fos = null;
        ObjectOutputStream fout = null;
        try{
            fos = new FileOutputStream(filename);
            fout = new ObjectOutputStream(fos);
            fout.writeObject(store);
            fout.close();
            return true;
        }catch(IOException ex){
            ex.printStackTrace();
            return false;
        }
     }

    public void output(ObjectName beanName, String attributeName, Object value) {
        final String id = MuninOutput.attributeName(beanName, attributeName);
        //if (!store.containsKey(id)) {
            //System.err.println("id " + id);
            CacheItem item = new CacheItem(beanName, attributeName, value);
            store.put(id,item);
        //}
    }

}
