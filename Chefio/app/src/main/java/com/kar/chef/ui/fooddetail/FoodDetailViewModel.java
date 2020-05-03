package com.kar.chef.ui.fooddetail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kar.chef.Common.Common;
import com.kar.chef.Model.CommentModel;
import com.kar.chef.Model.FoodModel;

public class FoodDetailViewModel extends ViewModel {

    private MutableLiveData<FoodModel> mutableLiveDataFood;
    private MutableLiveData<CommentModel> mutableLiveDataComment;

    public void setCommentModel (CommentModel commentModel)
    {
        if (mutableLiveDataComment != null)
            mutableLiveDataComment.setValue(commentModel);
    }
    public MutableLiveData<CommentModel> getMutableLiveDataComment() {
        return mutableLiveDataComment;
    }

    public FoodDetailViewModel () {
        mutableLiveDataComment = new MutableLiveData<>();
    }

        public MutableLiveData<FoodModel> getMutableLiveDataFood() {
        if(mutableLiveDataFood == null)
        {
            mutableLiveDataFood = new MutableLiveData<>();
        }
        mutableLiveDataFood.setValue(Common.selectedFood);
        return mutableLiveDataFood;
    }

    public void setFoodModel(FoodModel foodModel) {
        if (mutableLiveDataFood !=null)
        {
            mutableLiveDataFood.setValue(foodModel);
        }
    }
}