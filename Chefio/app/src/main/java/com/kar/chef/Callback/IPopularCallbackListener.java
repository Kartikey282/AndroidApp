package com.kar.chef.Callback;

import com.kar.chef.Model.PopularCategoryModel;

import java.util.List;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels);

    static void onPopularLoadFailed(String message) {

    }

}
