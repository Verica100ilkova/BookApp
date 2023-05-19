package mk.ukim.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import mk.ukim.bookapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    //view binding
    private ActivityRegisterBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth=FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        binding.backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, begin register
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();

            }
        });
    }

    private String name="", email="", password="";
    private void validateData() {
        //get data
        name=binding.nameEt.getText().toString().trim();
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString().trim();
        String cPassword=binding.cPasswordEt.getText().toString().trim();
        //validate data
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Invalid email pattern!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cPassword))
        {
            Toast.makeText(this, "Confirm Password", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(cPassword))
        {
            Toast.makeText(this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            createUserAccount();
        }



    }

    private void createUserAccount() {
        //show progress
        progressDialog.setMessage("Creating account");
        progressDialog.show();

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                       //account creation success, now add in firebase realtime database
                        updateUserInfo();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //account creating failed
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setTitle("Saving user info");

        //timestamp
        long timestamp=System.currentTimeMillis();

        //get current user id
        String uid=firebaseAuth.getUid();

        //setuip data to asdd in db
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); //add empty, I will do it later
        hashMap.put("userType", "user"); //possible values are user, admin
        hashMap.put("timestamp", timestamp);

        //srt data to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data added to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                        //since user acc is created so start dashboad of user
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //data failed adding to db
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}