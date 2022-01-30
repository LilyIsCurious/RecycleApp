package com.example.myrecycle.contract;

import android.annotation.SuppressLint;

import com.example.myrecycle.api.ApiService;
import com.example.myrecycle.model.TrashNewsResponse;
import com.llw.mvplibrary.base.BasePresenter;
import com.llw.mvplibrary.base.BaseView;
import com.llw.mvplibrary.network.NetworkApi;
import com.llw.mvplibrary.network.observer.BaseObserver;


/**
 * 主页面访问网络
 * Main page access network
 */
public class MainContract {

    public static class MainPresenter extends BasePresenter<MainView> {
        /**
         * 垃圾分类新闻
         * garbage sorting news
         *
         * @param num 数量
         */
        @SuppressLint("CheckResult")
        public void getTrashNews(Integer num) {
            ApiService service = NetworkApi.createService(ApiService.class,0);
            service.getTrashNews(num).compose(NetworkApi.applySchedulers(new BaseObserver<TrashNewsResponse>() {
                @Override
                public void onSuccess(TrashNewsResponse trashNewsResponse) {
                    if (getView() != null) {
                        getView().getTrashNewsResponse(trashNewsResponse);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    if (getView() != null) {
                        getView().getTrashNewsFailed(e);
                    }
                }
            }));
        }
    }

    public interface MainView extends BaseView {
        /**
         * 获取垃圾分类新闻返回
         *
         * @param response
         */
        void getTrashNewsResponse(TrashNewsResponse response);

        /**
         * 搜索物品异常返回
         *
         * @param throwable
         */
        void getTrashNewsFailed(Throwable throwable);
    }
}

