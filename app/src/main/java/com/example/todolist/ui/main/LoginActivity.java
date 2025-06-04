package com.example.todolist.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.databinding.ActivityLoginBinding;
import com.example.todolist.data.firebase.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuthManager authManager;

    private ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    authManager.handleGoogleSignInResult(result.getData(), new FirebaseAuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            hideLoading();
                            Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            startMainActivity();
                        }

                        @Override
                        public void onFailure(String error) {
                            hideLoading();
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    hideLoading();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager(this);

        // Check if user is already logged in
        if (authManager.isUserLoggedIn()) {
            startMainActivity();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        binding.btnGoogleSignIn.setOnClickListener(v -> {
            showLoading();
            Intent signInIntent = authManager.getGoogleSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Add animation to the logo
        binding.ivLogo.setScaleX(0.8f);
        binding.ivLogo.setScaleY(0.8f);
        binding.ivLogo.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(1000)
                .start();
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleSignIn.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnGoogleSignIn.setEnabled(true);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}