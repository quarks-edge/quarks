/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.file;

import java.io.File;

import quarks.connectors.file.FileStreams;
import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Watch a directory for files and convert their contents into a stream.
 */
public class FileReaderApp {
    private final String directory;
    private static final String baseLeafname = "FileSample";

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to an existing directory");
        FileReaderApp reader = new FileReaderApp(args[0]);
        reader.run();
    }
   
    /**
     * 
     * @param directory an existing directory to watch for file
     */
    public FileReaderApp(String directory) {
        File dir = new File(directory);
        if (!dir.exists())
            throw new IllegalArgumentException("directory doesn't exist");
        this.directory = directory;
    }
    
    public void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application / topology
        
        Topology t = tp.newTopology("FileSample consumer");

        // watch for files
        TStream<String> pathnames = FileStreams.directoryWatcher(t, () -> directory);
        
        // create a stream containing the files' contents.
        // use a preFn to include a separator in the results.
        // use a postFn to delete the file once its been processed.
        TStream<String> contents = FileStreams.textFileReader(pathnames,
                tuple -> "<PRE-FUNCTION> "+tuple, 
                (tuple,exception) -> {
                    // exercise a little caution in case the user pointed
                    // us at a directory with other things in it
                    if (tuple.contains("/"+baseLeafname+"_")) { 
                        new File(tuple).delete();
                    }
                    return null;
                });
        
        // print out what's being read
        contents.print();
        
        // run the application / topology
        System.out.println("starting the reader watching directory " + directory);
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
