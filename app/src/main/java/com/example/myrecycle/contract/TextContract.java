package com.example.myrecycle.contract;

import android.annotation.SuppressLint;

import com.example.myrecycle.api.ApiService;
import com.example.myrecycle.model.TrashResponse;
import com.llw.mvplibrary.base.BasePresenter;
import com.llw.mvplibrary.base.BaseView;
import com.llw.mvplibrary.network.NetworkApi;
import com.llw.mvplibrary.network.observer.BaseObserver;

/**
 * 手动文字输入页面访问网络
 * text input page access network
 */
public class TextContract {

    public static class TextPresenter extends BasePresenter<TextView> {
        /**
         * 搜索物品
         * search objects
         *
         * @param word 物品名
         */
        @SuppressLint("CheckResult")
        public void searchGoods(String word) {
            ApiService service = NetworkApi.createService(ApiService.class, 0);
            service.searchGoods(word).compose(NetworkApi.applySchedulers(new BaseObserver<TrashResponse>() {
                @Override
                public void onSuccess(TrashResponse groupResponse) {
                    if (getView() != null) {
                        getView().getSearchResponse(groupResponse);
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    if (getView() != null) {
                        getView().getSearchResponseFailed(e);
                    }
                }
            }));
        }
    }

    public interface TextView extends BaseView {
        /**
         * 搜索物品返回
         *
         * @param response
         */
        void getSearchResponse(TrashResponse response);

        /**
         * 搜索物品异常返回
         *
         * @param throwable
         */
        void getSearchResponseFailed(Throwable throwable);
    }
}
