package io.server.adindex;
import java.util.List;

public class AdsIndexServerImpl extends AdsIndexGrpc.AdsIndexImplBase{
    private String mMemcachedServer;
    private int mMemcachedPortal;
    private int mTFMemcachedPortal;
    private int mDFMemcachedPortal;
    private String mysql_host;
    private String mysql_db;
    private String mysql_user;
    private String mysql_pass;
    public AdsIndexServerImpl(String memcachedServer,int memcachedPortal,int tfMemcachedPortal, int dfMemcachedPortal,String mysqlHost,String mysqlDb,String user,String pass) {
        mMemcachedServer = memcachedServer;
        mMemcachedPortal = memcachedPortal;
        mTFMemcachedPortal = tfMemcachedPortal;
        mDFMemcachedPortal = dfMemcachedPortal;
        mysql_host = mysqlHost;
        mysql_db = mysqlDb;
        mysql_user = user;
        mysql_pass = pass;
    }
    @Override
    public void getAds(io.server.adindex.AdsRequest request,
                       io.grpc.stub.StreamObserver<io.server.adindex.AdsReply> responseObserver) {
        System.out.println("received requests number of query:" + request.getQueryCount());
        for(int i = 0; i < request.getQueryCount();i++) {
            Query query = request.getQuery(i);

            List<Ad>  adsCandidates = AdsSelector.
                    getInstance(mMemcachedServer, mMemcachedPortal,mTFMemcachedPortal, mDFMemcachedPortal, mysql_host, mysql_db,mysql_user, mysql_pass).
                    selectAds(query);
            AdsReply.Builder replyBuilder = AdsReply.newBuilder();
            for(Ad ad : adsCandidates) {
                if(ad.getRelevanceScore() > 0.07) {
                    replyBuilder.addAd(ad);
                }
            }
            AdsReply reply = replyBuilder.build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }


}
