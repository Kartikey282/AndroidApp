package com.kar.chef.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kar.chef.Callback.IRecyclerClickListener;
import com.kar.chef.Common.Common;
import com.kar.chef.EventBus.CategoryClick;
import com.kar.chef.Model.CategoryModel;
import com.kar.chef.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoriesAdapter extends RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder> {

    Context context;
    List<CategoryModel> categoryModelsList;

    public MyCategoriesAdapter(Context context, List<CategoryModel> categoryModelsList) {
        this.context = context;
        this.categoryModelsList = categoryModelsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_category_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(categoryModelsList.get(position).getImage())
                .into(holder.category_image);
        holder.category_name.setText(new StringBuilder(categoryModelsList.get(position).getName()));

        //event
        holder.setListener((view, pos) -> {
            Common.categorySelected = categoryModelsList.get(pos);
            EventBus.getDefault().postSticky(new CategoryClick(true,categoryModelsList.get(pos)));

         });

    }

    @Override
    public int getItemCount() {
        return categoryModelsList.size();
    }

    public class MyViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.img_category)
        ImageView category_image;
        @BindView(R.id.txt_category)
        TextView category_name;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (categoryModelsList.size() == 1)
            return Common.DEFAULT_COLUMN_COUNT;
        else
        {
            if (categoryModelsList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position > 1 && position == categoryModelsList.size()-1) ? Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;
        }
    }
}
