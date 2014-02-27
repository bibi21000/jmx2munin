package org.vafer.jmx;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.security.MessageDigest;

import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;
import java.util.Arrays;

import java.lang.System;

import javax.management.ObjectName;

import org.vafer.jmx.munin.MuninOutput;

public final class CacheOutput implements Output {
/* output data to cache
 *
 */
    public Hashtable store = new Hashtable();
    private String filename;
    private String cryptkey;
    private int debug;

    public CacheOutput(String url, String expression, String mcryptkey, int mdebug) {
        File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));
        File fileInDirectory = new File(tmpDirectory, sanitizeFilename(url,expression));
        cryptkey = mcryptkey;
        filename = fileInDirectory.getPath();
        debug=mdebug;
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
            if ((cryptkey!=null) && (cryptkey!="")) {
                SealedObject sealedObject = (SealedObject)fin.readObject();
                String algorithmName = sealedObject.getAlgorithm();
                Cipher cipher = Cipher.getInstance(algorithmName);
                cipher.init(Cipher.DECRYPT_MODE, secretKey());
                store = (Hashtable)sealedObject.getObject(cipher);
                if (debug>3) {
                    System.err.println("Read Crypted data");
                }
            } else {
                store = (Hashtable)fin.readObject();
                if (debug>3) {
                    System.err.println("Read plain data");
                }
            }
            fin.close();
            fis.close();
            return true;
        } catch (IOException ex){
            ex.printStackTrace();
            deleteFile();
            return false;
        } catch (ClassNotFoundException ex){
            ex.printStackTrace();
            deleteFile();
            return false;
        }catch(IllegalBlockSizeException ex){
            ex.printStackTrace();
            deleteFile();
            return false;
        }catch(NoSuchPaddingException ex){
            ex.printStackTrace();
            deleteFile();
            return false;
        }catch(InvalidKeyException ex){
            ex.printStackTrace();
            deleteFile();
            return false;
        }catch(BadPaddingException ex){
            ex.printStackTrace();
            deleteFile();
            return false;
        }catch(NoSuchAlgorithmException ex){
            ex.printStackTrace();
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

    public SecretKeySpec secretKey() {
        try {
            byte[] key = cryptkey.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
            return new SecretKeySpec(key, "AES");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return null;
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean storeInFile() {
        FileOutputStream fos = null;
        ObjectOutputStream fout = null;
        try{
            fos = new FileOutputStream(filename);
            fout = new ObjectOutputStream(fos);
            if ((cryptkey!=null) && (cryptkey!="")) {
                //Try to store cache from disk
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey());
                SealedObject sealedObject = new SealedObject(store, cipher);
                fout.writeObject(sealedObject);
                if (debug>3) {
                    System.err.println("Store crypted data");
                }
            } else {
                fout.writeObject(store);
                if (debug>3) {
                    System.err.println("Store plain data");
                }
            }
            fout.close();
            fos.close();
            return true;
        }catch(IOException ex){
            ex.printStackTrace();
            return false;
        }catch(InvalidKeyException ex){
            ex.printStackTrace();
            return false;
        }catch(NoSuchAlgorithmException ex){
            ex.printStackTrace();
            return false;
        }catch(IllegalBlockSizeException ex){
            ex.printStackTrace();
            return false;
        }catch(NoSuchPaddingException ex){
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
