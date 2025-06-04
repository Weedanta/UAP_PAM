package com.example.todolist.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
        binding.btnSignIn.setOnClickListener(v -> signInWithEmail());

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            showLoading();
            Intent signInIntent = authManager.getGoogleSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // Add animation to the logo
        binding.ivLogo.setScaleX(0.8f);
        binding.ivLogo.setScaleY(0.8f);
        binding.ivLogo.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(1000)
                .start();
    }

    private void signInWithEmail() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            binding.etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        }

        showLoading();

        authManager.signInWithEmailAndPassword(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                hideLoading();
                String displayName = user.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = user.getEmail().split("@")[0]; // Use email prefix as fallback
                }
                Toast.makeText(LoginActivity.this, "Welcome back " + displayName, Toast.LENGTH_SHORT).show();
                startMainActivity();
            }

            @Override
            public void onFailure(String error) {
                hideLoading();
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(android.R.layout.select_dialog_item, null);
        final android.widget.EditText etResetEmail = new android.widget.EditText(this);
        etResetEmail.setHint("Enter your email address");
        etResetEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        builder.setTitle("Reset Password")
                .setMessage("Enter your email address to receive a password reset link")
                .setView(etResetEmail)
                .setPositiveButton("Send Reset Email", (dialog, which) -> {
                    String email = etResetEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(LoginActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendPasswordResetEmail(email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        authManager.resetPassword(email, new FirebaseAuthManager.ResetPasswordCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignIn.setEnabled(false);
        binding.btnGoogleSignIn.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSignIn.setEnabled(true);
        binding.btnGoogleSignIn.setEnabled(true);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}