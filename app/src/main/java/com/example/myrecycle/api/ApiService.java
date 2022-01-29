package com.example.myrecycle.api;

import static com.example.myrecycle.utils.Constant.KEY;

import com.example.myrecycle.model.TrashNewsResponse;
import com.example.myrecycle.model.TrashResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API接口
 */
public interface ApiService {

    /**
     * 垃圾分类
     * @param word 物品名
     * @return TrashResponse 结果实体
     */
    @GET("/txapi/lajifenlei/index?key=" + KEY)
    Observable<TrashResponse> searchGoods(@Query("word") String word);

    /**
     * 垃圾分类新闻
     * @param num 数量
     * @return TrashNewsResponse 结果实体
     */
    @GET("/lajifenleinews/index?key=" + KEY)
    Observable<TrashNewsResponse> getTrashNews(@Query("num") Integer num);

}

