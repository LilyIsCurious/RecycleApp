package com.example.myrecycle.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.NestedScrollingChild;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myrecycle.R;
import com.example.myrecycle.adapter.DiscernResultAdapter;
import com.example.myrecycle.adapter.SearchGoodsAdapter;
import com.example.myrecycle.contract.ImageContract;
import com.example.myrecycle.model.TrashResponse;
import com.example.myrecycle.utils.Base64Util;
import com.example.myrecycle.utils.Constant;
import com.example.myrecycle.utils.SPUtils;
import com.google.firebase.auth.api.model.GetTokenResponse;
import com.iflytek.cloud.msc.util.FileUtil;
import com.llw.mvplibrary.mvp.MvpActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.annotations.Nullable;

/**
 * 图像输入物品进行垃圾分类
 * Image input items for garbage classification
 */
public class ImageInputActivity extends MvpActivity<ImageContract.ImagePresenter> implements ImageContract.ImageView, View.OnClickListener {

    private static final String TAG = "ImageInputActivity";
    /**
     * 打开相册
     * open album
     */
    private static final int OPEN_ALBUM_CODE = 100;
    /**
     * 打开相机
     * open camera
     */
    private static final int TAKE_PHOTO_CODE = 101;
    /**
     * 鉴权Toeken
     * Authentication token
     */
    private String accessToken;

    private Toolbar toolbar;
    private ImageView ivPicture;
    private EditText etImageUrl;
    private LinearLayout layRecognitionResult,layClassificationResult;
    private RecyclerView rvRecognitionResult,rvClassificationResult;
    private RxPermissions rxPermissions;
    private NestedScrollView nestedScrollingView;

    private File outputImage;

    /**
     * 初始化
     * initialize
     */
    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        ivPicture = findViewById(R.id.iv_picture);
        etImageUrl = findViewById(R.id.et_image_url);
        nestedScrollingView = findViewById(R.id.nestedScrollView);
        findViewById(R.id.btn_web_picture).setOnClickListener(this);
        findViewById(R.id.btn_open_album).setOnClickListener(this);
        findViewById(R.id.btn_take_photo).setOnClickListener(this);
        layRecognitionResult = findViewById(R.id.lay_recognition_result);
        layClassificationResult = findViewById(R.id.lay_classification_result);
        rvRecognitionResult = findViewById(R.id.rv_recognition_result);
        rvClassificationResult = findViewById(R.id.rv_classification_result);
        //设置页面状态栏
        setStatubar(this, R.color.white, true);
        back(toolbar, true);
        rxPermissions = new RxPermissions(this);
    }

    /**
     * 滑动到屏幕底部
     * scroll to the end
     */
    private void scrollToEnd() {
        nestedScrollingView.post(() -> {
            nestedScrollingView.fullScroll(View.FOCUS_DOWN);//滚到底部 scroll to the end
            //nestedScrollView.fullScroll(ScrollView.FOCUS_UP);//滚到顶部 scroll to the top
        });
    }


    @SuppressLint("CheckResult")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_web_picture://网络图片 image from the internet
                etImageUrl.setVisibility(View.VISIBLE);
                etImageUrl.setOnKeyListener((view, keyCode, keyEvent) -> {
                    if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        String webImageUrl = etImageUrl.getText().toString().trim();
                        String defaultWebImageUrl = "https://bce-baiyu.cdn.bcebos.com/14ce36d3d539b6004ef2e45fe050352ac65cb71e.jpeg";
                        String imageUrl = "".equals(webImageUrl) ?defaultWebImageUrl : webImageUrl;
                        //识别网络图片Url recognize url
                        showLoadingDialog();
                        Glide.with(context).load(imageUrl).into(ivPicture);
                        mPresenter.getDiscernResult(accessToken,null,imageUrl);
                        etImageUrl.setVisibility(View.GONE);
                    }
                    return false;
                });
                break;

            case R.id.btn_open_album://image from album
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    rxPermissions.request(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(grant -> {
                                if (grant) {
                                    //获得权限 get permission
                                    openAlbum();
                                } else {
                                    showMsg("未获取到权限");
                                }
                            });
                } else {
                    openAlbum();
                }
                break;

            case R.id.btn_take_photo://拍照图片 take picture
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    rxPermissions.request(
                            Manifest.permission.CAMERA)
                            .subscribe(grant -> {
                                if (grant) {
                                    //获得权限 get permission
                                    turnOnCamera();
                                } else {
                                    showMsg("未获取到权限");
                                }
                            });
                } else {
                    turnOnCamera();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 打开相机
     * open camera
     */
    private void turnOnCamera() {
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("HH_mm_ss");
        String filename = timeStampFormat.format(new Date());
        //创建File对象 create file object
        outputImage = new File(getExternalCacheDir(), "takePhoto" + filename + ".jpg");
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(this,
                    "com.llw.goodtrash.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //打开相机 open camera
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO_CODE);
    }


    /**
     * 打开相册
     * open album
     */
    private void openAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_ALBUM_CODE);
    }



    @Override
    public void initData(Bundle savedInstanceState) {
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_image_input;
    }

    @Override
    protected ImageContract.ImagePresenter createPresenter() {
        return new ImageContract.ImagePresenter();
    }


    /**
     * 获取鉴权Token成功返回
     * @param response GetTokenResponse
     */
    @Override
    public void getTokenResponse(GetTokenResponse response) {
        if (response != null) {
            //鉴权Token
            accessToken = response.getAccess_token();
            //过期时间 秒
            long expiresIn = response.getExpires_in();
            //当前时间 秒
            long currentTimeMillis = System.currentTimeMillis() / 1000;
            //放入缓存
            SPUtils.putString(Constant.TOKEN, accessToken, this);
            SPUtils.putLong(Constant.GET_TOKEN_TIME, currentTimeMillis, this);
            SPUtils.putLong(Constant.TOKEN_VALID_PERIOD, expiresIn, this);
        } else {
            showMsg("Token为null");
        }
    }

    /**
     * 获取Token失败返回
     * @param throwable 异常
     */
    @Override
    public void getTokenFailed(Throwable throwable) {
        Log.d(TAG, "Token获取失败："+throwable.toString());
    }


    /**
     * 图片识别成功返回
     * @param response GetDiscernResultResponse
     */
    @Override
    public void getDiscernResultResponse(GetDiscernResultResponse response) {
        if(response == null){
            hideLoadingDialog();
            showMsg("未获得相应的识别结果");
            return;
        }
        ivPicture.setVisibility(View.VISIBLE);
        List<GetDiscernResultResponse.ResultBean> result = response.getResult();
        if (result != null && result.size() > 0) {
            //显示识别结果
            showDiscernResult(result);
        } else {
            hideLoadingDialog();
            showMsg("未获得相应的识别结果");
        }
    }

    /**
     * 图片识别成功失败
     * @param throwable 异常
     */
    @Override
    public void getDiscernResultFailed(Throwable throwable) {
        Log.d(TAG, "图片识别失败："+throwable.toString());
    }


    /**
     * 搜索物品进行垃圾分类成功返回
     *
     * @param response TrashResponse
     */
    @Override
    public void getSearchResponse(TrashResponse response) {
        //请求成功  进行数据的渲染 Request to render data successfully
        if (response.getCode() == Constant.SUCCESS_CODE) {
            List<TrashResponse.NewslistBean> result = response.getNewslist();
            if (result != null && result.size() > 0) {
                //显示分类结果 display classification result
                showClassificationResult(result);
            } else {
                showMsg("触及到了知识盲区");
            }
        } else {
            hideLoadingDialog();
            showMsg(response.getMsg());
        }
    }

    @Override
    public void getSearchResponseFailed(Throwable throwable) {
        Log.d(TAG, "搜索物品进行垃圾分类失败：" + throwable.toString());
    }

    /**
     * 显示物品分类结果
     * @param result
     */
    private void showClassificationResult(List<TrashResponse.NewslistBean> result) {
        SearchGoodsAdapter adapter = new SearchGoodsAdapter(R.layout.item_search_rv,result);
        rvClassificationResult.setLayoutManager(new LinearLayoutManager(context));
        rvClassificationResult.setAdapter(adapter);
        //隐藏加载
        hideLoadingDialog();
        //显示弹窗
        layClassificationResult.setVisibility(View.VISIBLE);
        //滑动到屏幕底部
        scrollToEnd();
    }


    /**
     * 获取鉴权Token
     */
    private String getAccessToken() {
        String token = SPUtils.getString(Constant.TOKEN, null, this);
        if (token == null) {
            //访问API获取接口 Access API to get interface
            mPresenter.getToken();
        } else {
            //则判断Token是否过期 Then judge whether the token has expired
            if (isTokenExpired()) {
                //过期 expired
                mPresenter.getToken();
            } else {
                accessToken = token;
            }
        }
        return accessToken;
    }

    /**
     * Token是否过期
     *
     * @return
     */
    private boolean isTokenExpired() {
        //获取Token的时间
        long getTokenTime = SPUtils.getLong(Constant.GET_TOKEN_TIME, 0, this);
        //获取Token的有效时间
        long effectiveTime = SPUtils.getLong(Constant.TOKEN_VALID_PERIOD, 0, this);
        //获取当前系统时间
        long currentTime = System.currentTimeMillis() / 1000;

        return (currentTime - getTokenTime) >= effectiveTime;
    }

    /**
     * 显示识别的结果列表
     *
     * @param result
     */
    private void showDiscernResult(List<GetDiscernResultResponse.ResultBean> result) {
        DiscernResultAdapter adapter = new DiscernResultAdapter(R.layout.item_distinguish_result_rv, result);
        //添加列表Item点击 Add list item Click
        adapter.setOnItemChildClickListener((adapter1, view, position) -> {
            showLoadingDialog();
            //垃圾分类 garbage sorting
            mPresenter.searchGoods(result.get(position).getKeyword());
        });

        rvRecognitionResult.setLayoutManager(new LinearLayoutManager(this));
        rvRecognitionResult.setAdapter(adapter);
        //隐藏加载 Hide loading
        hideLoadingDialog();
        //显示弹窗 Display pop-up window
        layRecognitionResult.setVisibility(View.VISIBLE);
        layClassificationResult.setVisibility(View.GONE);
        //滑动到屏幕底部
        scrollToEnd();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            showLoadingDialog();
            if (requestCode == OPEN_ALBUM_CODE) {
                //打开相册返回 open album return
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                final Uri imageUri = Objects.requireNonNull(data).getData();
                Cursor cursor = getContentResolver().query(imageUri, filePathColumns, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumns[0]);
                //获取图片路径 Get picture path
                String imagePath = cursor.getString(columnIndex);
                cursor.close();
                //识别 recognize
                localImageDiscern(imagePath);
            }
        } 	else if (requestCode == TAKE_PHOTO_CODE) {
            //拍照返回picture return
            String imagePath = outputImage.getAbsolutePath();
            //识别 recognize
            localImageDiscern(imagePath);
        }

        else {
            showMsg("什么都没有");
        }
    }

    /**
     * 本地图片识别
     * Local image recognition
     */
    private void localImageDiscern(String imagePath) {
        try {
            String token = getAccessToken();
            //通过图片路径显示图片 Display pictures through picture path
            Glide.with(this).load(imagePath).into(ivPicture);
            //按字节读取文件 Read file by byte
            byte[] imgData = FileUtil.readFileByBytes(imagePath);
            //字节转Base64 Byte to Base64
            String imageBase64 = Base64Util.encode(imgData);
            //本地图片识别 Local image recognition
            mPresenter.getDiscernResult(token, imageBase64, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

