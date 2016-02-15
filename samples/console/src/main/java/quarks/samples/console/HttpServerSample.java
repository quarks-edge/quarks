/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.console;

import quarks.console.server.HttpServer;

public class HttpServerSample {
    public static void main(String[] args)  {

        try {
        HttpServer server = HttpServer.getInstance();
        server.startServer();
        String consolePath = server.getConsoleUrl();
        System.out.println("Point your browser to :");
        System.out.println(consolePath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
