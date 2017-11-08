package kkt.com.joggers;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity{
    private String TAG = "FUCKING USA";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance(); //getcurrentuser가 null이면 로그아웃



        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                    Log.d(TAG, "onAuthStateChanged(: signed in :"+user.getUid());
                else
                    Log.d(TAG, "onAuthStateChanged(: signed out ");
            }
        };
    }

    public void onButtonLogin(View v){
        final String email = ((EditText)findViewById(R.id.id)).getText().toString();
        String pass = ((EditText)findViewById(R.id.pass)).getText().toString();

        // TODO

        mAuth.signInWithCredential()
        mAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInwithEmail:onComlete : " + task.isSuccessful());
                        if(!task.isSuccessful()){ //fail
                            Log.w(TAG, "signInWithEmail",task.getException());
                            Toast.makeText(LoginActivity.this, "\"Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                        else{ //success
                            //TODO
                            Intent intent = new Intent();
                            intent.putExtra("id", email);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
    }

}
