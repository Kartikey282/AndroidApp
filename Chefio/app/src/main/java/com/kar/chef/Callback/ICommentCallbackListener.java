package com.kar.chef.Callback;

import com.kar.chef.Model.CommentModel;

import java.util.List;

public interface ICommentCallbackListener {
    void onCommentLoadSuccess (List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
