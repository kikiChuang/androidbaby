package timeroute.androidbaby.api;

/**
 * Created by chinesejar on 17-7-14.
 */

public class ApiFactory {

    protected static final Object monitor = new Object();
    static FeedApi feedApiSingleton = null;

    public static FeedApi getFeedApiSingleton(){
        synchronized (monitor){
            if(feedApiSingleton == null){
                feedApiSingleton = new ApiRetrofit().getFeedApiService();
            }
            return feedApiSingleton;
        }
    }
}