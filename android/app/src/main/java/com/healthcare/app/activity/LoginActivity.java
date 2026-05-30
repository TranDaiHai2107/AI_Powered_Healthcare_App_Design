package com.healthcare.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityLoginBinding;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    setLoading(false);
                    Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnSignIn.setOnClickListener(v -> attemptLogin());
        binding.btnGoogleSignIn.setOnClickListener(v -> startGoogleSignIn());

        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void startGoogleSignIn() {
        setLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) { setLoading(false); return; }

                    boolean isNewUser = authResult.getAdditionalUserInfo() != null
                            && authResult.getAdditionalUserInfo().isNewUser();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    if (isNewUser) {
                        // Create Firestore profile for new Google users
                        String patientId = "PT-" + user.getUid().substring(0, 6).toUpperCase();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", user.getUid());
                        userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "");
                        userData.put("email", user.getEmail() != null ? user.getEmail() : "");
                        userData.put("phone", "");
                        userData.put("address", "");
                        userData.put("patientId", patientId);

                        db.collection("users").document(user.getUid()).set(userData)
                                .addOnSuccessListener(v -> savePrefsAndNavigate(user, db))
                                .addOnFailureListener(e -> savePrefsAndNavigate(user, db));
                    } else {
                        savePrefsAndNavigate(user, db);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void savePrefsAndNavigate(FirebaseUser user, FirebaseFirestore db) {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = (doc.exists() && doc.getString("name") != null)
                            ? doc.getString("name") : (user.getDisplayName() != null ? user.getDisplayName() : "User");
                    SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("userId", user.getUid())
                            .putString("userName", name)
                            .apply();
                    setLoading(false);
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    SharedPreferences prefs = getSharedPreferences("healthcare_prefs", MODE_PRIVATE);
                    prefs.edit().putString("userId", user.getUid()).apply();
                    setLoading(false);
                    navigateToHome();
                });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        if (email.isEmpty()) { binding.tilEmail.setError("Email is required"); return; }
        if (password.isEmpty()) { binding.tilPassword.setError("Password is required"); return; }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        savePrefsAndNavigate(user, FirebaseFirestore.getInstance());
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSignIn.setEnabled(!loading);
        binding.btnGoogleSignIn.setEnabled(!loading);
        binding.etEmail.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
    }
}
