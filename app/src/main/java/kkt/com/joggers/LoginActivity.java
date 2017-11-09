package kkt.com.joggers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "Joggers.LoginActivity";

    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 로그인 Button 설정
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Firebase Authentication 객체 생성
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 이전 실행 시 로그인되어 있으면 바로 MainActivity를 실행한다
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            startMain(currentUser);
    }

    void makeToast(GoogleSignInAccount account) {
        if (account != null) {
            signInButton.setVisibility(Button.INVISIBLE); //로그인 버튼 숨김
            Toast.makeText(this, "구글 계정으로 로그인 되었습니다", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "구글 계정 로그인을 실패하였습니다", Toast.LENGTH_SHORT).show();
        }
    }

    void startMain(FirebaseUser user) {
        String string = String.format("%s 님 환영합니다", user.getDisplayName());
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        LoginActivity.this.startActivity(intent);
        finish();
    }

    public static FirebaseAuth getMAuth() {
        // 다른 Activity에서 Firebase Authentication 정보를 사용할 수 있다
        return mAuth;
    }

    /* == 로그인 처리 함수들 == */

    /* 1 로그인 버튼 클릭 */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            signIn();
        }
    }

    /* 2 구글 로그인 액티비티 실행 */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /* 3 구글 로그인 액티비티 실행결과 생성 (task) */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /* 4 구글 로그인 액티비티 실행결과(task)로부터 구글계정정보 생성 (account) */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            makeToast(account); //Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "구글 로그인 결과:실패 code=" + e.getStatusCode());
            makeToast(null);
        }
    }

    /* 5 구글계정정보(account)로 파이어베이스 인증 (credential의 completeListener) */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "Firebase Auth 구글ID로 로그인:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "Firebase Auth 구글ID로 로그인 결과:성공");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startMain(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "Firebase Auth 구글ID로 로그인 결과:실패", task.getException());
                            Toast.makeText(LoginActivity.this, "로그인 실패",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
