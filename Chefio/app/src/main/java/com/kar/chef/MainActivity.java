package com.kar.chef;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kar.chef.Common.Common;
import com.kar.chef.Model.UserModel;
import com.kar.chef.Remote.RetrofitCloudClient;
import com.kar.chef.Remote.TCloudFunctions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable disposable = new CompositeDisposable();
    private TCloudFunctions cloudFunctions;
    private ArrayList<Disposable> compositeDisposable;

    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        disposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init ();
    }



    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());


        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        cloudFunctions = RetrofitCloudClient.getInstance().create(TCloudFunctions.class);
        listener = firebaseAuth1 -> {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            FirebaseUser user =firebaseAuth.getCurrentUser();
                            if (user !=null)
                            {
                                checkUserFromFirebase(user);
                            } else
                            {
                                phoneLogin();
                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(MainActivity.this,"You must enable this permission to use app",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    }).check();
        };
        //Account is already logged in
        //Toast.makeText(MainActivity.this,"Already Login" , Toast.LENGTH_SHORT).show();
        //this::onAuthStateChanged;
    }

    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            Toast.makeText(MainActivity.this,"You already registed", Toast.LENGTH_SHORT).show();
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel);
                        }
                        else
                        {
                            showRegisterDialog(user);
                        }

                        dialog.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please Fill information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText edt_name = (EditText)itemView.findViewById(R.id.edt_name);
        EditText edt_address = (EditText)itemView.findViewById(R.id.edt_address);
        EditText edt_phone = (EditText)itemView.findViewById(R.id.edt_phone);
        TextView txt_address = (TextView)itemView.findViewById(R.id.txt2_address_detail);
        RadioButton edt_build1=(RadioButton)itemView.findViewById(R.id.build1);
        RadioButton edt_build2=(RadioButton)itemView.findViewById(R.id.build2);
        RadioButton edt_build3=(RadioButton)itemView.findViewById(R.id.build3);
        RadioButton edt_build4=(RadioButton)itemView.findViewById(R.id.build4);



        //Set
        edt_phone.setText(user.getPhoneNumber());

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(edt_address.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your address", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserModel userModel = new UserModel();
                userModel.setUid(user.getUid());
                userModel.setName(edt_name.getText().toString());
                userModel.setAddress(edt_address.getText().toString());
                userModel.setPhone(edt_phone.getText().toString());
                if (edt_build1.isChecked()) {
                    userModel.setBuilding(edt_build1.getText().toString());
                }
                else if (edt_build2.isChecked())
                {
                    userModel.setBuilding(edt_build2.getText().toString());
                }
                else if (edt_build3.isChecked())
                {
                    userModel.setBuilding(edt_build3.getText().toString());
                }
                else {
                    userModel.setBuilding(edt_build4.getText().toString());
                }

                userRef.child(user.getUid()).setValue(userModel)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    dialogInterface.dismiss();
                                    Toast.makeText(MainActivity.this, "Congratulation ! Register success", Toast.LENGTH_SHORT).show();
                                    goToHomeActivity(userModel);

                                }
                            }
                        });
            }
        });
        builder.setView(itemView);


        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goToHomeActivity(UserModel userModel) {

          Common.currentUser = userModel;
          //important you need always assign value before use
           startActivity(new Intent(MainActivity.this,HomeActivity.class));
           finish();


        //Start activity soon
    }

    private void phoneLogin() {

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).build(),
                APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == APP_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText(this,"Failed to sign in !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onAuthStateChanged(FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            //Account is already logged in
            //Toast.makeText(MainActivity.this,"Already Login" , Toast.LENGTH_SHORT).show();
            checkUserFromFirebase(user);
        } else {

            phoneLogin();
        }
    }
}
