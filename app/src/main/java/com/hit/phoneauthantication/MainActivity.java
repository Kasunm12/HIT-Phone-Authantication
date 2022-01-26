package com.hit.phoneauthantication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hit.phoneauthantication.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


//    biewBinding
    private ActivityMainBinding binding;

    //if code send failed,will used to resend code OTP

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private static final String TAG = "MAIN_TAG";
    private FirebaseAuth firebaseAuth;

    //init progress dialog
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(R.layout.activity_main);
        setContentView(binding.getRoot());

        binding.phoneLl.setVisibility(View.VISIBLE); //Show phone layout
        binding.phoneLl.setVisibility(View.GONE);//hind code layout

        firebaseAuth = FirebaseAuth.getInstance();

        //init progress dialog
        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {


                pd.dismiss();
                Toast.makeText(MainActivity.this,"" +e.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, forceResendingToken);

                Log.d(TAG,"OnCodeSent"+ verificationId);

                mVerificationId = verificationId;
                forceResendingToken = token;
                pd.dismiss();

                binding.phoneLl.setVisibility(View.GONE);
                binding.codeLl.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this,"Verification code sent...",Toast.LENGTH_SHORT).show();

                binding.codeSentDescription.setText("Please type the verification code we sent \nto"+binding.phoneEt.getText().toString().trim());


            }
        };
        
        binding.phoneContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = binding.phoneEt.getText().toString().trim();

                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(MainActivity.this,"Please Enter phone number...",Toast.LENGTH_SHORT).show();
                }
                else{
                    startPhoneNumberVerification(phone);
                }
            }
        });

        binding.resendCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phone = binding.phoneEt.getText().toString().trim();

                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(MainActivity.this,"Please Enter phone number...",Toast.LENGTH_SHORT).show();
                }
                else{
                    resendVerificationCode(phone,forceResendingToken);
                }

            }
        });

        binding.CodeSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = binding.codeEt.getText().toString().trim();

                if(TextUtils.isEmpty(code)){
                    Toast.makeText(MainActivity.this,"Please Enter verification code...",Toast.LENGTH_SHORT).show();
                }
                else{
                    VerifyPhoneNumberWithCode(mVerificationId,code);
                }
                
            }
        });
    }


    private void startPhoneNumberVerification(String phone){

        pd.setMessage("Verifying Phone Number");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token){

        pd.setMessage("Resending Code");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void VerifyPhoneNumberWithCode(String verificationId, String code) {

        pd.setMessage("Verifying Code");
        pd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        pd.setMessage("Logging In");
        
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //successfully signed in
                       pd.dismiss();
                       String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                       Toast.makeText(MainActivity.this,"Logged In as" +phone,Toast.LENGTH_SHORT).show();

                       startActivity(new Intent(MainActivity.this,ProfileActivity.class));

                    }
                })
        .addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {

                //failed signing in
                pd.dismiss();
//                String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                Toast.makeText(MainActivity.this,"" +e.getMessage(),Toast.LENGTH_SHORT).show();
                
            }
        });
    }


}