package org.vafer.jmx.munin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.vafer.jmx.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public final class Munin {

    @Parameter(names = "-list", description = "show as list")
    private boolean list;

    @Parameter(names = "-url", description = "jmx url", required = true)
    private String url;

    @Parameter(names = "-username", description = "remote username")
    private String username;

    @Parameter(names = "-cryptkey", description = "key used to crypt data in local store")
    private String cryptkey;

    @Parameter(names = "-password", description = "remote password")
    private String password;

    @Parameter(names = "-query", description = "query expression", required = true)
    private List<String> queries = new ArrayList<String>();

    @Parameter(names = "-enums", description = "file string to enum config")
    private String enumsPath;

    @Parameter(names = "-attribute", description = "attributes to return")
    private List<String> attributes = new ArrayList<String>();

    @Parameter(names = "-ttl", description = "cache time to live")
    private int ttl;

    @Parameter(names = "-debug", description = "output debug messages on error console")
    private int debug;

    private void run() throws Exception {
        //System.out.println("Do not crypt data '" + cryptkey + "'");

        //if (cryptkey != null) {
        //    cryptkey = username+password+cryptkey;
        //}

        final Filter filter;
        if (attributes == null || attributes.isEmpty()) {
            filter = new NoFilter();
        } else {
            filter = new MuninAttributesFilter(attributes);
        }

        final Enums enums = new Enums();
        if (enumsPath != null) {
            enums.load(enumsPath);
        }

        final Output output;
        if (list) {
            output = new ListOutput();
        } else {
            output = new MuninOutput(enums);
        }

        Map credentials = null;
        if (username!="") {
            credentials = formatCredentials();
        }

        for(String query : queries) {
            new Query().run(url, credentials, query, filter, output, ttl, debug, cryptkey);
        }
    }

    //Function to format Credential information.
    private Map formatCredentials()
    {
        Map env = new HashMap();
        env.put("jmx.remote.credentials", new String[]{username, password});
        return env;
    }

    public static void main(String[] args) throws Exception {
        Munin m = new Munin();

        JCommander cli = new JCommander(m);
        try {
            cli.parse(args);
        } catch(Exception e) {
            cli.usage();
            System.exit(1);
        }

        m.run();
    }
}
