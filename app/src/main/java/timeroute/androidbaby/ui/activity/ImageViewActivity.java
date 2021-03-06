package timeroute.androidbaby.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import butterknife.Bind;
import me.relex.circleindicator.CircleIndicator;
import timeroute.androidbaby.R;
import timeroute.androidbaby.ui.adapter.ViewPagerImageAdapter;
import timeroute.androidbaby.ui.base.IBaseActivity;
import timeroute.androidbaby.ui.presenter.ImageViewPresenter;
import timeroute.androidbaby.ui.view.IImageViewView;
import timeroute.androidbaby.ui.view.ImageViewClickListener;
import timeroute.androidbaby.widget.TouchImageView;

public class ImageViewActivity extends IBaseActivity<IImageViewView, ImageViewPresenter> implements IImageViewView {

    private static final String TAG = "ImageViewActivity";
    private RxPermissions rxPermissions;
    private ArrayList<String> arrayImages;
    private ViewPagerImageAdapter imageAdapter;
    private ImageViewClickListener imageViewClickListener;
    private boolean isShow = true;

    @Bind(R.id.layoutProgress)
    LinearLayout layoutProgress;
    @Bind(R.id.textProgress)
    TextView textViewProgress;
    @Bind(R.id.viewPagerImages)
    ViewPager viewPager;
    @Bind(R.id.indicator)
    CircleIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setTracker(TAG);
        rxPermissions = new RxPermissions(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setElevation(0);

        Intent intent = getIntent();
        int index = intent.getIntExtra("index", -1);
        arrayImages = new ArrayList<>();
        String[] images = intent.getStringArrayExtra("images");
        for (int i = 0; i < images.length; i++) {
            arrayImages.add(images[i]);
        }
        imageViewClickListener = () -> {
            if(isShow){
                getSupportActionBar().hide();
            }else {
                getSupportActionBar().show();
            }
            isShow = !isShow;
        };
        imageAdapter = new ViewPagerImageAdapter(this, arrayImages, imageViewClickListener);
        viewPager.setAdapter(imageAdapter);
        viewPager.setCurrentItem(index);
        indicator.setViewPager(viewPager);
    }

    @Override
    public boolean canBack() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imageview, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_download:
                rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if(granted){
                                mPresenter.saveImage(arrayImages.get(viewPager.getCurrentItem()));
                            }else {
                                Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_SHORT).show();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected ImageViewPresenter createPresenter() {
        return new ImageViewPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_image_view;
    }

    @Override
    public void setDisplayProgress(boolean flag) {
        if(flag){
            layoutProgress.setVisibility(View.VISIBLE);
        }else {
            layoutProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void setProgressIndex(int index) {
        textViewProgress.setText(String.format(getString(R.string.progress), index));
    }
}
