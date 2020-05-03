package com.kar.chef.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kar.chef.Callback.IRecyclerClickListener;
import com.kar.chef.Common.Common;
import com.kar.chef.Database.CartDataSource;
import com.kar.chef.Database.CartDatabase;
import com.kar.chef.Database.CartItem;
import com.kar.chef.Database.LocalCartDataSource;
import com.kar.chef.EventBus.CounterCartEvent;
import com.kar.chef.EventBus.FoodItemClick;
import com.kar.chef.Model.FoodModel;
import com.kar.chef.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyFoodlistAdapter extends RecyclerView.Adapter<MyFoodlistAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public MyFoodlistAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_food_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.img_food_image);
        holder.txt_food_price.setText(new StringBuilder("$")
        .append(foodModelList.get(position).getPrice()));
        holder.txt_food_name.setText(new StringBuilder("")
        .append(foodModelList.get(position).getName()));

        //Event
        holder.setListener((view, position1) -> {
            Common.selectedFood = foodModelList.get(position1);
            Common.selectedFood.setKey(String.valueOf(position1));
            EventBus.getDefault().postSticky(new FoodItemClick(true,foodModelList.get(position1)));
        });

        holder.img_quick_cart.setOnClickListener(view -> {
            CartItem cartItem = new CartItem();
            cartItem.setUid((Common.currentUser.getUid()));
            cartItem.setUserPhone(Common.currentUser.getPhone());

            cartItem.setFoodId(foodModelList.get(position).getId());
            cartItem.setFoodName(foodModelList.get(position).getName());
            cartItem.setFoodImage (foodModelList.get (position).getImage ());
            cartItem.setFoodPrice (Double.valueOf(String.valueOf(foodModelList.get(position).getPrice())));
            cartItem.setFoodQuantity(1);
            cartItem.setFoodPrice(0.0);
            cartItem.setFoodAddon("DEFAULT");
            cartItem.setFoodSize("DEFAULT");

            cartDataSource.getItemWithAllOptionsInCart (Common.currentUser.getUid (),
                    cartItem.getFoodId (),
                    cartItem.getFoodSize(),
                    cartItem.getFoodAddon () )
                    .subscribeOn (Schedulers.io())
                    .observeOn (AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<CartItem>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(CartItem cartItemFromDB) {
                            if (cartItemFromDB.equals (cartItem))
                            {
                                //Already in database, just update
                                cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice () );
                                cartItemFromDB. setFoodAddon (cartItem. getFoodAddon () ) ;
                                cartItemFromDB.setFoodSize(cartItem.getFoodSize ());
                                cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                                cartDataSource.updateCartItem(cartItemFromDB)
                                        .subscribeOn (Schedulers.io())
                                        .observeOn (AndroidSchedulers.mainThread ())
                                        .subscribe(new SingleObserver<Integer>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(Integer integer) {
                                                Toast.makeText(context,  "update cart success ", Toast.LENGTH_SHORT).show() ;
                                                EventBus.getDefault() .postSticky (new CounterCartEvent(  true));

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Toast.makeText(context,"[UPDATE CART] "+e.getMessage (), Toast.LENGTH_SHORT).show() ;

                                            }
                                        });
                            }
                            else
                            {
                                // Item not available in cart before ,insert new
                                compositeDisposable. add (cartDataSource.insertOrReplaceAll (cartItem)
                                    .subscribeOn (Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread() )
                                    .subscribe(() -> {
                                        Toast.makeText(context,  "add to  cart success ", Toast.LENGTH_SHORT).show() ;
                                        EventBus.getDefault() .postSticky (new CounterCartEvent( true));
                                    },throwable -> {
                                        Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show() ;
                                    }));
                            }


                        }

                        @Override
                        public void onError(Throwable e) {
                            if(e.getMessage () .contains ("empty"))
                            {
                                //Default, if Cart is empty this code will be fired
                                compositeDisposable. add (cartDataSource.insertOrReplaceAll (cartItem)
                                        .subscribeOn (Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread() )
                                        .subscribe(() -> {
                                            Toast.makeText(context,  "add to  cart success ", Toast.LENGTH_SHORT).show() ;
                                            EventBus.getDefault() .postSticky (new CounterCartEvent( true));
                                        },throwable -> {
                                            Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show() ;
                                        }));

                            }

                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.img_fav)
        ImageView img_fav;
        @BindView(R.id.img_quick_cart)
        ImageView img_quick_cart;

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
        public void onClick(View view) {
            listener.onItemClickListener(view,getAdapterPosition());
        }
    }
}
