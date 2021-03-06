package timeroute.androidbaby.ui.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.qiniu.android.storage.UploadManager;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timeroute.androidbaby.R;
import timeroute.androidbaby.api.exception.ApiException;
import timeroute.androidbaby.api.exception.ExceptionEngine;
import timeroute.androidbaby.bean.user.ImageToken;
import timeroute.androidbaby.bean.user.Profile;
import timeroute.androidbaby.support.MyObserver;
import timeroute.androidbaby.ui.base.BasePresenter;
import timeroute.androidbaby.ui.view.IMineView;
import timeroute.androidbaby.util.SharedPreferenceUtils;

/**
 * Created by chinesejar on 17-7-14.
 */

public class MinePresenter extends BasePresenter<IMineView> {

    private String token;

    private Context context;
    private IMineView mineView;

    private SharedPreferenceUtils sharedPreferenceUtils;

    public MinePresenter(Context context){
        this.context = context;
        sharedPreferenceUtils = new SharedPreferenceUtils(this.context, "user");
        token = sharedPreferenceUtils.getString("token");
    }

    public void getToken(){
        mineView = getView();
        if(mineView != null){
            userApi.getImageToken("JWT "+token)
                    .onErrorResumeNext(throwable -> Observable.error(ExceptionEngine.handleException(throwable)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ImageToken>() {
                        @Override
                        protected void onError(ApiException ex) {
                            Toast.makeText(context, ex.getDisplayMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ImageToken imageToken) {
                            displayToken(imageToken);
                        }
                    });
        }
    }

    public void getNotification(){
        mineView = getView();
        if(mineView != null){
            feedApi.getNotification("JWT "+token)
                    .onErrorResumeNext(throwable -> Observable.error(ExceptionEngine.handleException(throwable)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Response<Object>>() {
                        @Override
                        protected void onError(ApiException ex) {

                        }

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(Response<Object> response) {
                            if(response.code() == 200) {
                                mineView.setNotification(true);
                            }
                        }
                    });
        }
    }

    private void displayToken(ImageToken imageToken) {
        UploadManager uploadManager = new UploadManager();
        File data = mineView.getAvatar();
        File compressedImage = null;
        try {
            compressedImage = new Compressor(this.context)
                    .setMaxWidth(400)
                    .setMaxHeight(400)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFile(data);
        }catch (IOException e){

        }
        if(compressedImage != null){
            String name = String.valueOf(sharedPreferenceUtils.getInt("id"))+"_"+System.currentTimeMillis()+".jpg";
            uploadManager.put(compressedImage, name, imageToken.getToken(), (key, info, response) -> {
                if(info.isOK()) {
                    Log.i("qiniu", "Upload Success");
                    Profile profile = new Profile();
                    profile.setAvatar(baseAvatarUrl+name);
                    int id = sharedPreferenceUtils.getInt("id");
                    userApi.putProfile("JWT "+token, id, profile)
                            .onErrorResumeNext(throwable -> Observable.error(ExceptionEngine.handleException(throwable)))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new MyObserver<Void>() {
                                @Override
                                protected void onError(ApiException ex) {
                                    Toast.makeText(context, ex.getDisplayMessage(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onNext(Void aVoid) {
                                    Toast.makeText(context, context.getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                                    sharedPreferenceUtils.setString("avatar", baseAvatarUrl+name);
                                    mineView.setAvatar(baseAvatarUrl+name);
                                }
                            });
                } else {
                    Toast.makeText(context, context.getString(R.string.update_fail), Toast.LENGTH_SHORT).show();
                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                }
            }, null);
        }
    }

}
