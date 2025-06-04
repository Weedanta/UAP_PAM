package com.example.todolist.data.firebase;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.todolist.R;
import com.example.todolist.data.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseAuthManager {
    private static final String TAG = "FirebaseAuthManager";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private Activity activity;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public interface ResetPasswordCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public FirebaseAuthManager(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    // Email and Password Authentication
    public void createUserWithEmailAndPassword(String name, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                // Update user profile with name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            if (profileTask.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                saveUserToFirestore(user);
                                                callback.onSuccess(user);
                                            } else {
                                                Log.w(TAG, "Error updating user profile", profileTask.getException());
                                                // Still save to Firestore even if profile update fails
                                                saveUserToFirestore(user);
                                                callback.onSuccess(user);
                                            }
                                        });
                            } else {
                                callback.onFailure("Failed to get user information");
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void signInWithEmailAndPassword(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void resetPassword(String email, ResetPasswordCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            callback.onSuccess();
                        } else {
                            Log.w(TAG, "Error sending reset email", task.getException());
                            String errorMessage = getAuthErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    // Google Authentication (existing methods)
    public Intent getGoogleSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public void handleGoogleSignInResult(Intent data, AuthCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken(), callback);
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            callback.onFailure("Google sign in failed: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserToFirestore(user);
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
<<<<<<< HEAD:app/src/main/java/com/example/todolist/data/firebase/FIrebaseAuthManager.java
                            callback.onFailure("Authentication failed: " + task.getException().getMessage());
=======
                            String errorMessage = getAuthErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
>>>>>>> b61f967 (final commit):app/src/main/java/com/example/todolist/data/firebase/FirebaseAuthManager.java
                        }
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            User user = new User(
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName(),
                    firebaseUser.getEmail(),
                    firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : ""
            );

            db.collection("users")
                    .document(firebaseUser.getUid())
                    .set(user)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile saved"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error saving user profile", e));
        }
    }

    public void signOut(Runnable callback) {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(activity, task -> {
            if (callback != null) {
                callback.run();
            }
        });
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private String getAuthErrorMessage(Exception exception) {
        if (exception == null) {
            return "Authentication failed";
        }

        String errorCode = exception.getMessage();
        if (errorCode == null) {
            return "Authentication failed";
        }

        // Handle common Firebase Auth error codes
        if (errorCode.contains("ERROR_WEAK_PASSWORD")) {
            return "Password is too weak. Please choose a stronger password.";
        } else if (errorCode.contains("ERROR_INVALID_EMAIL")) {
            return "Invalid email address format.";
        } else if (errorCode.contains("ERROR_EMAIL_ALREADY_IN_USE")) {
            return "This email is already registered. Please use a different email or sign in.";
        } else if (errorCode.contains("ERROR_USER_NOT_FOUND")) {
            return "No account found with this email. Please check your email or create a new account.";
        } else if (errorCode.contains("ERROR_WRONG_PASSWORD")) {
            return "Incorrect password. Please try again.";
        } else if (errorCode.contains("ERROR_USER_DISABLED")) {
            return "This account has been disabled. Please contact support.";
        } else if (errorCode.contains("ERROR_TOO_MANY_REQUESTS")) {
            return "Too many failed attempts. Please try again later.";
        } else if (errorCode.contains("ERROR_NETWORK_REQUEST_FAILED")) {
            return "Network error. Please check your internet connection.";
        } else {
            return "Authentication failed: " + errorCode;
        }
    }
}