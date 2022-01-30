package com.example.myrecycle.contract;

import android.annotation.SuppressLint;

import com.example.myrecycle.api.ApiService;
import com.example.myrecycle.model.TrashResponse;
import com.llw.mvplibrary.base.BasePresenter;
import com.llw.mvplibrary.base.BaseView;
import com.llw.mvplibrary.network.NetworkApi;
import com.llw.mvplibrary.network.observer.BaseObserver;

/**
 * 语音文字输入页面访问网络
 * voice recording page access network
 */
public class VoiceContract {

    public static class VoicePresenter extends BasePresenter<VoiceView> {
        /**
         * 搜索物品
         * search object
         *
         * @param word 物品名
         */
        @SuppressLint("CheckResult")
        public void searchGoods(String word) {
            ApiService service = NetworkApi.createService(ApiService.class);
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

    public interface VoiceView extends BaseView {
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

