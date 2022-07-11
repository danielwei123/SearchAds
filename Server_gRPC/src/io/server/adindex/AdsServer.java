package io.server.adindex;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Logger;

public class AdsServer {
    private static final Logger logger = Logger.getLogger(AdsServer.class.getName());
    private Server server;
    private int port;
    private String mMemcachedServer;
    private int mMemcachedPortal;
    private int mTFMemcachedPortal;
    private int mDFMemcachedPortal;
    private String mysql_host;
    private String mysql_db;
    private String mysql_user;
    private String mysql_pass;
    public  AdsServer(int _port, String _memcachedServer,
                      int _memcachedPortal,
                      int _tfMemcachedPortal,
                      int _dfMemcachedPortal,
                      String _mysql_host,
                      String _mysql_db, String _mysql_user,
                      String _mysql_pass) {
        port = _port;
        mMemcachedServer = _memcachedServer;
        mMemcachedPortal = _memcachedPortal;
        mTFMemcachedPortal = _tfMemcachedPortal;
        mDFMemcachedPortal = _dfMemcachedPortal;
        mysql_host = _mysql_host;
        mysql_db = _mysql_db;
        mysql_user = _mysql_user;
        mysql_pass = _mysql_pass;
    }

    private void start() throws IOException {
    /* The port on which the server should run */
        server = ServerBuilder.forPort(port)
                .addService(new AdsIndexServerImpl(mMemcachedServer, mMemcachedPortal,
                        mTFMemcachedPortal, mDFMemcachedPortal,
                        mysql_host, mysql_db,
                        mysql_user, mysql_pass))
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                AdsServer.this.stop();
                System.err.println("ads index server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String portStr = args[0];
        int port = Integer.parseInt(portStr);
        String  memcachedServer = args[1];
        int memcachedPortal = Integer.parseInt(args[2]);
        int tfMemcachedPortal = Integer.parseInt(args[3]);
        int dfMemcachedPortal = Integer.parseInt(args[4]);
        String mysql_host = args[5];
        String mysql_db = args[6];
        String mysql_user = args[7];
        String mysql_pass = args[8];

        final AdsServer server = new AdsServer(port, memcachedServer,
                memcachedPortal, tfMemcachedPortal, dfMemcachedPortal, mysql_host, mysql_db,
                mysql_user, mysql_pass);
        server.start();
        server.blockUntilShutdown();
    }





}
