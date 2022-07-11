package io.server.adindex;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClient;

public class AdsSelector {
    private static AdsSelector instance = null;
    //private int EXP = 7200;
    private int numDocs = 10840;
    private String mMemcachedServer;
    private int mMemcachedPortal;
    private int mTFMemcachedPortal;
    private int mDFMemcachedPortal;
    private String mysql_host;
    private String mysql_db;
    private String mysql_user;
    private String mysql_pass;
    private Boolean enableTFIDF;
    MemcachedClient cache;
    MemcachedClient tfCacheClient;
    MemcachedClient dfCacheClient;

    protected AdsSelector(String memcachedServer,int memcachedPortal,int tfMemcachedPortal, int dfMemcachedPortal,String mysqlHost,String mysqlDb,String user,String pass)
    {
        mMemcachedServer = memcachedServer;
        mMemcachedPortal = memcachedPortal;
        mTFMemcachedPortal = tfMemcachedPortal;
        mDFMemcachedPortal = dfMemcachedPortal;
        mysql_host = mysqlHost;
        mysql_db = mysqlDb;
        mysql_user = user;
        mysql_pass = pass;
        String address = mMemcachedServer + ":" + mMemcachedPortal;
        try {
            cache = new MemcachedClient(new ConnectionFactoryBuilder().setDaemon(true).setFailureMode(FailureMode.Retry).build(), AddrUtil.getAddresses(address));
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        enableTFIDF = true;
        String tf_address = mMemcachedServer + ":" + mTFMemcachedPortal;
        String df_address = mMemcachedServer + ":" + mDFMemcachedPortal;
        try
        {
            tfCacheClient = new MemcachedClient(new ConnectionFactoryBuilder().setDaemon(true).setFailureMode(FailureMode.Retry).build(), AddrUtil.getAddresses(tf_address));
            dfCacheClient = new MemcachedClient(new ConnectionFactoryBuilder().setDaemon(true).setFailureMode(FailureMode.Retry).build(), AddrUtil.getAddresses(df_address));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static AdsSelector getInstance(String memcachedServer,int memcachedPortal,int tfMemcachedPortal, int dfMemcachedPortal, String mysqlHost,String mysqlDb,String user,String pass) {
        if(instance == null) {
            instance = new AdsSelector(memcachedServer, memcachedPortal, tfMemcachedPortal, dfMemcachedPortal,mysqlHost,mysqlDb,user,pass);
        }
        return instance;
    }

    public List<Ad> selectAds(Query query)
    {
        List<Ad> adList = new ArrayList<Ad>();
        HashMap<Long,Integer> matchedAds = new HashMap<Long,Integer>();
        try {

            for(int i = 0; i < query.getTermList().size();i++)
            {
                String queryTerm = query.getTerm(i);
                System.out.println("selectAds queryTerm = " + queryTerm);
                @SuppressWarnings("unchecked")
                Set<Long>  adIdList = (Set<Long>)cache.get(queryTerm);
                if(adIdList != null && adIdList.size() > 0)
                {
                    for(Object adId : adIdList)
                    {
                        Long key = (Long)adId;
                        if(matchedAds.containsKey(key))
                        {
                            int count = matchedAds.get(key) + 1;
                            matchedAds.put(key, count);
                        }
                        else
                        {
                            matchedAds.put(key, 1);
                        }
                    }
                }
            }
            for(Long adId:matchedAds.keySet())
            {
                System.out.println("selectAds adId = " + adId);
                MySQLAccess mysql = new MySQLAccess(mysql_host, mysql_db, mysql_user, mysql_pass);
                Ad.Builder  ad = mysql.getAdData(adId);
                double relevanceScore = matchedAds.get(adId) * 1.0 / ad.getKeyWordsList().size();
                double relevanceScoreTFIDF = getRelevanceScoreByTFIDF(adId, ad.getKeyWordsList().size(), query.getTermList());
                if(enableTFIDF) {
                    ad.setRelevanceScore(relevanceScoreTFIDF);

                } else {
                    ad.setRelevanceScore(relevanceScore);
                }
                System.out.println("relevanceScore = " + relevanceScore);
                ad.setRelevanceScore(relevanceScore);
                System.out.println("selectAds pClick = " + ad.getPClick());
                System.out.println("selectAds relevanceScore = " + ad.getRelevanceScore());
                adList.add(ad.build());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return adList;
    }
    //TF*IDF = log(numDocs / (docFreq + 1)) * sqrt(tf) * (1/sqrt(docLength))
    private double calculateTFIDF(Long adId, String term, int docLength) {
        String tfKey = adId.toString() + "_" + term;
        System.out.println("tfKey = " + tfKey);
        System.out.println("dfKey = " + term);

        String tf = (String)tfCacheClient.get(tfKey);
        System.out.println("tf = " + tf);

        String df =  (String)dfCacheClient.get(term);
        System.out.println("df=" + df);

        if(tf != null && df != null) {
            int tfVal = Integer.parseInt(tf);
            int dfVal = Integer.parseInt(df);
            double dfScore = Math.log10(numDocs * 1.0 / (dfVal + 1));
            double tfScore = Math.sqrt(tfVal);
            double norm = Math.sqrt(docLength);
            double tfidfScore = (dfScore*tfScore) / norm;
            return tfidfScore;
        }
        return 0.0;
    }

    private double getRelevanceScoreByTFIDF(Long adId, int numOfKeyWords, List<String> queryTerms) {
        double relevanceScore = 0.0;
        for(String term : queryTerms) {
            relevanceScore += calculateTFIDF(adId, term, numOfKeyWords);
        }
        return relevanceScore;
    }
}
