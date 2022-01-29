package com.example.myrecycle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myrecycle.adapter.TrashNewsAdapter;
import com.example.myrecycle.contract.MainContract;
import com.example.myrecycle.model.TrashNewsResponse;
import com.example.myrecycle.ui.ImageInputActivity;
import com.example.myrecycle.ui.NewsDetailsActivity;
import com.example.myrecycle.ui.TextInputActivity;
import com.example.myrecycle.utils.Constant;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;
import com.llw.mvplibrary.mvp.MvpActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity<Banner> extends MvpActivity<MainContract.MainPresenter> implements MainContract.MainView {

    //轮播
    private Banner banner;
    private String TAG;
    private RecyclerView rvNews;
    private List<TrashNewsResponse.NewslistBean> mList = new ArrayList<>();
    private TrashNewsAdapter mAdapter;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;

    /**
     * 页面初始化
     */
    private void initView() {
        banner = (Banner) findViewById(R.id.banner);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        appBarLayout = findViewById(R.id.appbar_layout);
        rvNews = findViewById(R.id.rv_news);

        //伸缩偏移量监听
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {//收缩时
                    collapsingToolbarLayout.setTitle("垃圾分类");
                    isShow = true;
                } else if (isShow) {//展开时
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });
        //设置列表
        mAdapter = new TrashNewsAdapter(R.layout.item_trash_new_rv, mList);
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            //跳转到新闻详情页面
            Intent intent = new Intent(context, NewsDetailsActivity.class);
            intent.putExtra("url", mList.get(position).getUrl());
            startActivity(intent);

        });
        rvNews.setLayoutManager(new LinearLayoutManager(context));
        rvNews.setAdapter(mAdapter);
        //请求垃圾分类新闻数据
            TrashNewsResponse response = new Gson().fromJson(Constant.LOCAL_NEWS_DATA, TrashNewsResponse.class);
            mList.clear();
            mList.addAll(response.getNewslist());
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void initData(Bundle savedInstanceState) {
        //页面初始化
        initView();
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    /**
     * 进入文字输入页面
     */
    public void jumpTextInput(View view) {
        gotoActivity(TextInputActivity.class);
    }

    /**
     * 进入声音输入页面
     */
    public void jumpVoiceInput(View view) {

    }

    /**
     * 进入Activity
     * @param clazz 目标Activity
     */
    private void gotoActivity(Class<?> clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
    }

    /**
     * 进入图像输入页面
     */
    public void jumpImageInput(View view) {
        gotoActivity(ImageInputActivity.class);
    }

    @Override
    protected MainContract.MainPresenter createPresenter() {
        return new MainContract.MainPresenter();
    }

    /**
     * 获取垃圾分类新闻成功返回
     *
     * @param response
     */
    @Override
    public void getTrashNewsResponse(TrashNewsResponse response) {
        if (response.getCode() == Constant.SUCCESS_CODE) {
            List<TrashNewsResponse.NewslistBean> list = response.getNewslist();
            if (list.size() > 0) {
                //数据显示
                showBanner(list);//轮播显示
                showList();//新闻列表显示
            } else {
                showMsg("垃圾分类新闻为空");
            }
        }else{
                showMsg(response.getMsg());
            }
        }

            /**
             * 显示轮播图
             *
             * @param list
             */
            public void showBanner (List < TrashNewsResponse.NewslistBean > list) {
                banner.setAdapter(new BannerImageAdapter<TrashNewsResponse.NewslistBean>(list) {
                    @Override
                    public void onBindView(BannerImageHolder holder, TrashNewsResponse.NewslistBean data, int position, int size) {
                        //显示轮播图片
                        Glide.with(holder.itemView)
                                .load(data.getPicUrl())
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
                                .into(holder.imageView);
                    }
                })
                        .addBannerLifecycleObserver(this)//添加生命周期观察者
                        .setIndicator(new CircleIndicator(this));
            }

            /**
             * 显示新闻列表
             *
             * @param list
             */
            private void showList (List < TrashNewsResponse.NewslistBean > list) {
                mList.clear();
                mList.addAll(list);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}











/*import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.myrecycle.adapter.SearchGoodsAdapter;
import com.example.myrecycle.contract.MainContract;
import com.example.myrecycle.model.TrashResponse;
import com.example.myrecycle.utils.Constant;
import com.llw.mvplibrary.mvp.MvpActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends MvpActivity<MainContract.MainPresenter> implements MainContract.MainView{
    private static final String TAG = "MainActivity";
    private EditText etGoods;//输入框
    private ImageView ivClear;//清空输入框
    private RecyclerView rvResult;//结果显示列表
    private List<TrashResponse.NewslistBean> newslistBeanList = new ArrayList<>();//数据列表
    private SearchGoodsAdapter searchGoodsAdapter;//结果列表适配器

    /**
     * 页面初始化
     */
   /* private void initView() {
        etGoods = findViewById(R.id.et_goods);
        ivClear = findViewById(R.id.iv_clear);
        rvResult = findViewById(R.id.rv_result);
        //配置适配器  设置布局和数据源
        searchGoodsAdapter = new SearchGoodsAdapter(R.layout.item_search_rv, newslistBeanList);
        //设置列表的布局管理器
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        //列表item点击事件
        searchGoodsAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            showMsg("点击了" + newslistBeanList.get(position).getName());
        });
        //设置列表适配器
        rvResult.setAdapter(searchGoodsAdapter);

        //设置输入监听
        etGoods.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() < 1) {
                    ivClear.setVisibility(View.INVISIBLE);
                } else {
                    ivClear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //设置动作监听
        etGoods.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                String word = etGoods.getText().toString().trim();
                if (word.isEmpty()) {
                    showMsg("请输入物品名");
                } else {
                    //显示加载弹窗
                    showLoadingDialog();
                    //控制输入法
                    controlInputMethod();
                    //请求接口
                    mPresenter.searchGoods(word);
                }
                return true;
            }
            return false;
        });
        //清空输入框内容
        ivClear.setOnClickListener(v -> {
            controlInputMethod();
            etGoods.setText("");
        });
    }

    /**
     * 控制输入法
     * 当输入法打开时关闭，关闭时弹出
     */
    /*private void controlInputMethod() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected MainContract.MainPresenter createPresenter() {
        return new MainContract.MainPresenter();
    }

    /**
     * 搜索物品返回数据
     *
     * @param response
     */
    /*@Override
    public void getSearchResponse(TrashResponse response) {
        //隐藏加载弹窗
        hideLoadingDialog();
        if (response.getCode() == Constant.SUCCESS_CODE) {
            //请求成功  进行数据的渲染
            if (response.getNewslist() != null && response.getNewslist().size() > 0) {
                newslistBeanList.clear();
                newslistBeanList.addAll(response.getNewslist());
                //刷新适配器
                searchGoodsAdapter.notifyDataSetChanged();
            } else {
                showMsg("触及到了知识盲区");
            }
        } else {
            //显示请求接口失败的原因
            showMsg(response.getMsg());
        }
    }

    /**
     * 搜索物品失败返回
     *
     * @param throwable 异常信息
     */
    /*@Override
    public void getSearchResponseFailed(Throwable throwable) {
        hideLoadingDialog();
        Log.e(TAG, throwable.toString());
    }

    @Override
    public void initData(Bundle savedInstanceState){
        initView();
    }

    private void getRecognitionResult(String goods) {
        //使用Get异步请求
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                //拼接访问地址
                .url("http://api.tianapi.com/txapi/lajifenlei/index?key=6660c63ac336c0b1f6425e11b511a466&word=" + goods)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (response.isSuccessful()) {//回调的方法执行在子线程。
                    Log.d("result: ", result);
                }
            }
        });
    }
}
*/