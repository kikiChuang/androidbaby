package timeroute.androidbaby.bean.user;

import java.io.Serializable;

/**
 * Created by chinesejar on 17-7-18.
 */

public class ImageToken implements Serializable {
    private String token;

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }
}