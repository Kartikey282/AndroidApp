package com.kar.chef;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.andremion.counterfab.CounterFab;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kar.chef.Common.Common;
import com.kar.chef.Database.CartDataSource;
import com.kar.chef.Database.CartDatabase;
import com.kar.chef.Database.LocalCartDataSource;
import com.kar.chef.EventBus.BestDealItemClick;
import com.kar.chef.EventBus.CategoryClick;
import com.kar.chef.EventBus.CounterCartEvent;
import com.kar.chef.EventBus.FoodItemClick;
import com.kar.chef.EventBus.HideFABCart;
import com.kar.chef.EventBus.PopularCategoryClick;
import com.kar.chef.Model.CategoryModel;
import com.kar.chef.Model.FoodModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private CartDataSource cartDataSource;
    android.app.AlertDialog dialog;




    @BindView(R.id.fab)
    CounterFab fab;


    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource((CartDatabase.getInstance(this).cartDAO()));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,R.id.nav_food_list,R.id.nav_cart)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront(); //Fixed

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey, ",Common.currentUser.getName(),txt_user);

        countCartItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId())
        {
            case R.id.nav_home:
                 navController.navigate(R.id.nav_home);
                  break;

            case R.id.nav_menu:
                 navController.navigate(R.id.nav_menu);
                 break;

            case R.id.nav_cart:
                 navController.navigate(R.id.nav_cart);
                 break;
            case R.id.nav_sign_out:
                 signOut();
                 break;
        }
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Do You Really Want To Sign Out?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //eventbus


    //@Override
    //protected void onStart() {
      //  super.onStart();
       // EventBus.getDefault().register(this);
    //}

    //@Override
    //protected void onStop() {
      //  EventBus.getDefault().unregister(this);
       // super.onStop();

    //}

    //@Subscribe()
    //public void onCategorySelected(CategoryClick event)
    //{
        //if (event.isSuccess())
        //{
            //Toast.makeText(this, "Click to"+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        //}
    //}
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    // UI updates must run on MainThread
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event)
    {
    if (event.isSuccess())
    {
        navController.navigate(R.id.nav_food_list);
      //Toast.makeText(this, "Click to"+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
    }
    }



    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_food_detail);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent (HideFABCart event)
    {
        if (event.isHidden())
        {
            countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter (CounterCartEvent event)
    {
        if (event.isSuccess())
        {
            fab.hide();
        }
        else
        {
            fab.show();
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick (BestDealItemClick event)
    {
        if (event.getBestDealModel() !=null)
        {
            dialog.show();

            FirebaseDatabase.getInstance()
                .getReference ( "Category")
                .child(event.getBestDealModel() .getMenu_id ())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);

                            //LOAD food
                            FirebaseDatabase.getInstance()
                                    .getReference ( "Category")
                                    .child(event.getBestDealModel() .getMenu_id ())
                                    .child("foods")
                                    .orderByChild("id")
                                    .equalTo(event.getBestDealModel().getFood_id())
                                    .limitToLast(1)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists())
                                            {
                                                for (DataSnapshot itemSnapShot:dataSnapshot.getChildren())
                                                {
                                                    Common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                }
                                                navController.navigate(R.id.nav_food_detail);
                                            }else {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "Item does't Exists", Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            dialog.dismiss();
                                            Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }else
                        {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this,"Item does't exists!",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }
        else
        {
            fab.show();
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick (PopularCategoryClick event)
    {
        if (event.getPopularCategoryModel() !=null)
        {
            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference ( "Category")
                    .child(event.getPopularCategoryModel() .getMenu_id ())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);

                                //LOAD food
                                FirebaseDatabase.getInstance()
                                        .getReference ( "Category")
                                        .child(event.getPopularCategoryModel() .getMenu_id ())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists())
                                                {
                                                    for (DataSnapshot itemSnapShot:dataSnapshot.getChildren())
                                                    {
                                                        Common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }else {
                                                    dialog.dismiss();
                                                    Toast.makeText(HomeActivity.this, "Item does't Exists", Toast.LENGTH_SHORT).show();

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }else
                            {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this,"Item does't exists!",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else
        {
            fab.show();
        }
    }


    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned zero")) {
                            Toast.makeText(HomeActivity.this, "[COUNT CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            fab.setCount(0);
                        }
                    }
                });
    }
}


