package com.kar.chef.ui.fooddetail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kar.chef.Common.Common;
import com.kar.chef.Database.CartDataSource;
import com.kar.chef.Database.CartDatabase;
import com.kar.chef.Database.CartItem;
import com.kar.chef.Database.LocalCartDataSource;
import com.kar.chef.EventBus.CounterCartEvent;
import com.kar.chef.Model.AddonModel;
import com.kar.chef.Model.CommentModel;
import com.kar.chef.Model.FoodModel;
import com.kar.chef.Model.SizeModel;
import com.kar.chef.R;
import com.kar.chef.ui.comments.CommentFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {

    private FoodDetailViewModel foodDetailViewModel;

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable ();



    private Unbinder unbinder;
    private android.app.AlertDialog waitingDialog;
    private BottomSheetDialog addonBottomSheetDialog;

    //View need inflate
    ChipGroup chip_group_addon;

    EditText edt_search;

    @BindView(R.id.img_food)
    ImageView img_food;
    @BindView(R.id.btn_cart)
    CounterFab btnCart;
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.number_button)
    ElegantNumberButton number_button;
    @BindView(R.id.rattingBar)
    RatingBar rattingBar;
    @BindView (R.id.rdi_group_size)
    RadioGroup rdi_group_size;
    @BindView(R.id. img_add_addon)
    ImageView img_add_on;
    @BindView (R.id.chip_group_user_selected_addon)
    ChipGroup chip_group_user_selected_addon;


    Button btnShowComment;
    @OnClick(R.id.btn_rating)
    void onRatingButtonClick()
    {
        showDialogRating();
    }

    @OnClick (R. id.btn_cart)
    void onCartItemAdd ()
    {
        CartItem cartItem = new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone (Common.currentUser.getPhone());

        cartItem.setFoodId (Common.selectedFood.getId());
        cartItem.setFoodName (Common.selectedFood.getName ());
        cartItem. setFoodImage (Common.selectedFood.getImage () );
        cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice ())));
        cartItem.setFoodQuantity (Integer.valueOf(number_button.getNumber()));
        cartItem.setFoodExtraPrice(Common.CalculateExtraPrice (Common.selectedFood.getUserSelectedSize (),
                Common.selectedFood.getUserSelectedAddon()));
        if (Common.selectedFood.getUserSelectedAddon () != null)
            cartItem. setFoodAddon (new Gson().toJson (Common.selectedFood.getUserSelectedAddon ()));
        else
            cartItem. setFoodAddon ("Default");
        if(Common.selectedFood.getUserSelectedSize() != null)
            cartItem.setFoodSize (new Gson().toJson (Common.selectedFood.getUserSelectedSize()));
        else
            cartItem.setFoodSize("Default");

        cartDataSource.getItemWithAllOptionsInCart (Common. currentUser.getUid(),
                cartItem.getFoodId (),
                cartItem.getFoodSize (),
                cartItem.getFoodAddon ())
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
                                            Toast.makeText(getContext(),  "update cart success ", Toast.LENGTH_SHORT).show() ;
                                            EventBus.getDefault() .postSticky (new CounterCartEvent(  true));

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext() ,"[UPDATE CART] "+e.getMessage (), Toast.LENGTH_SHORT).show() ;

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
                                        Toast.makeText(getContext(),  "add to  cart success ", Toast.LENGTH_SHORT).show() ;
                                        EventBus.getDefault() .postSticky (new CounterCartEvent( true));
                                    },throwable -> {
                                        Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show() ;
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
                                        Toast.makeText(getContext(),  "add to  cart success ", Toast.LENGTH_SHORT).show() ;
                                        EventBus.getDefault() .postSticky (new CounterCartEvent( true));
                                    },throwable -> {
                                        Toast.makeText(getContext(), "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show() ;
                                    }));

                        }

                    }
                });
    }

    @OnClick(R.id.btnShowComment)
    void onShowCommentButtonClick() {
        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager(),"CommentFragment");
    }
    @OnClick(R.id. img_add_addon)
    void onAddonClick () {
        if (Common.selectedFood.getAddon() != null) {

            displayAddonList();  // Show all addon options
            addonBottomSheetDialog.show();
        }
    }

    private void displayAddonList() {
        if (Common.selectedFood.getAddon ().size() > 0) {
            chip_group_addon.clearCheck();  //clear check all views
            chip_group_addon.removeAllViews();
            edt_search.addTextChangedListener(this);

            //Add all view
            for (AddonModel addonModel:Common. selectedFood.getAddon () )
            {

                    Chip chip = (Chip) getLayoutInflater().inflate (R.layout.layout_addon_item, null);
                    chip.setText (new StringBuilder (addonModel.getName()) .append(" (+$")
                            .append (addonModel.getPrice ()). append (")"));
                    chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b)
                    {
                        if (Common.selectedFood.getUserSelectedAddon() == null)
                            Common.selectedFood.setUserSelectedAddon (new ArrayList<>());
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                    chip_group_addon.addView(chip);


            }
        }
    }


    private void showDialogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder( getContext());

        builder.setTitle("Rating Food");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating,  null);

        RatingBar ratingBar = (RatingBar)itemView.findViewById(R.id.rating_bar);
        EditText edt_comment = (EditText)itemView.findViewById(R.id.edt_comment);

        builder.setView (itemView);

        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();

        });


        builder.setPositiveButton ( "OK", (dialogInterface, i) -> {
            CommentModel commentModel = new CommentModel();
            commentModel.setName (Common.currentUser.getName());
            commentModel.setUid (Common.currentUser.getUid());
            commentModel.setComment(edt_comment.getText().toString());
            commentModel.setRatingValue(ratingBar.getRating ());
            Map<String, Object> serverTimestamp = new HashMap<>();
            serverTimestamp.put("timeStamp", ServerValue.TIMESTAMP);
            commentModel.setCommentTimeStamp(serverTimestamp);


            foodDetailViewModel.setCommentModel(commentModel);

        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                ViewModelProviders.of(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);
       unbinder= ButterKnife.bind(this,root);
        initViews();
        foodDetailViewModel.getMutableLiveDataFood().observe(getViewLifecycleOwner(), foodModel -> {
            displayInfo(foodModel);
        });
        foodDetailViewModel.getMutableLiveDataComment ().observe(getViewLifecycleOwner(),commentModel -> {
            submitRatingToFirebase(commentModel);
        });
        return root;
    }

    private void initViews() {
        cartDataSource = new LocalCartDataSource
                (CartDatabase.getInstance (getContext ()).cartDAO ());

        waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        addonBottomSheetDialog = new BottomSheetDialog (getContext(), R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate (R.layout.layout_addon_display,null);
        chip_group_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search=(EditText) layout_addon_display.findViewById (R.id. edt_search);
        addonBottomSheetDialog.setContentView (layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener (dialogInterface ->  {
            displayUserSelectedAddon();
            calculateTotalPrice ();
        });


    }

    private void displayUserSelectedAddon() {
        if(Common.selectedFood.getUserSelectedAddon () !=null  &&
                Common.selectedFood.getUserSelectedAddon().size() > 0) {
            chip_group_user_selected_addon.removeAllViews();// clear all view already aded
            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon()) //ADD all available addodn to the list
            {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$").append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {  //Remove When user select delete
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_selected_addon.addView(chip);
            }
        } else
            chip_group_user_selected_addon.removeAllViews ();
    }

    private void submitRatingToFirebase(CommentModel commentModel) {

        waitingDialog.show();
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        //After submit to CommentRef , we will update value aveger in food
                        addRatingToFood(commentModel.getRatingValue());
                    }
                    waitingDialog.dismiss();
                });
    }

    private void addRatingToFood(float ratingValue) {
        FirebaseDatabase.getInstance()
                .getReference (Common. CATEGORY_REF)
                .child (Common.categorySelected.getMenu_id()) //Select Category
                .child("food") // Select array 1 ise "Foods of this category
                .child(Common.selectedFood.getKey())// Because food item is array
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            FoodModel foodModel = dataSnapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectedFood.getKey());//Dont forget set it

                            //Apply rating
                            if (foodModel.getRatingValue() == null)
                                foodModel.setRatingValue(0d);// d= D lower case
                            if (foodModel.getRatingCount()==null)
                                foodModel.setRatingCount(0l); // l = L lower case
                            double sumRating = foodModel.getRatingValue()+ratingValue;
                            long ratingCount = foodModel.getRatingCount()+1;

                            Map<String,Object> updateData = new HashMap<>();
                            updateData.put("ratingValue",sumRating);
                            updateData.put("ratingCount",ratingCount);

                            foodModel.setRatingCount(ratingCount);
                            foodModel.setRatingValue(sumRating);

                            dataSnapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(task -> {
                                        waitingDialog.dismiss();
                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(getContext(), "Thank You !", Toast.LENGTH_SHORT).show();
                                            Common.selectedFood = foodModel;
                                            foodDetailViewModel.setFoodModel(foodModel);


                                        }

                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        waitingDialog.dismiss();
                        Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));

        if (foodModel.getRatingValue() != null)
            rattingBar.setRating(foodModel.getRatingValue().floatValue() / foodModel.getRatingCount());

        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectedFood.getName());

        //Size
        for (SizeModel sizeModel : Common.selectedFood.getSize()) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b)
                    Common.selectedFood.setUserSelectedSize(sizeModel);
                calculateTotalPrice(); // Update price
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( 0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f);
            radioButton.setLayoutParams (params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag (sizeModel.getPrice());

            rdi_group_size.addView(radioButton) ;

        }

        if (rdi_group_size.getChildCount() > 0) {
            RadioButton radioButton = (RadioButton) rdi_group_size.getChildAt(0);
            radioButton.setChecked(true); // Default first select
        }

        calculateTotalPrice() ;
    }

    private void calculateTotalPrice() {

        double totalPrice = Double.parseDouble (Common.selectedFood.getPrice().toString ()),displayPrice=0.0;

        //Addon
        if (Common. selectedFood.getUserSelectedAddon () != null && Common.selectedFood.getUserSelectedAddon ().size() >0)
            for (AddonModel addonModel : Common.selectedFood.getUserSelectedAddon ())
                totalPrice +=Double. parseDouble (addonModel.getPrice ().toString());
        //size
        if (Common.selectedFood.getUserSelectedSize() !=null)
            totalPrice += Double.parseDouble(Common. selectedFood.getUserSelectedSize().getPrice().toString());


        displayPrice = totalPrice * (Integer.parseInt(number_button.getNumber())) ;
        displayPrice= Math.round (displayPrice* 100.0/100.0);

        food_price.setText (new StringBuilder ("").append (Common. formatPrice (displayPrice)).toString());

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();

        for (AddonModel addonModel:Common. selectedFood.getAddon () )
        {

                Chip chip = (Chip) getLayoutInflater().inflate (R.layout.layout_addon_item, null);
                chip.setText (new StringBuilder (addonModel.getName()) .append(" (+$")
                        .append (addonModel.getPrice ()). append (")")); chip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b)
                {
                    if (Common.selectedFood.getUserSelectedAddon() == null)
                        Common.selectedFood.setUserSelectedAddon (new ArrayList<>());
                    Common.selectedFood.getUserSelectedAddon().add(addonModel);
                }
            });
                chip_group_addon.addView(chip);


        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        //Nothing
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    }
