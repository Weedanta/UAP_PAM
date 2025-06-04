package com.example.todolist.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.todolist.R;
import com.example.todolist.databinding.ActivityMainBinding;
import com.example.todolist.data.firebase.FirebaseAuthManager;
import com.example.todolist.ui.fragment.HomeFragment;
import com.example.todolist.ui.fragment.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private FirebaseAuthManager authManager;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager(this);

        // Check if user is logged in
        if (!authManager.isUserLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login");
            startLoginActivity();
            return;
        }

        Log.d(TAG, "MainActivity created for logged in user");
        setupBottomNavigation();

        // Set default fragment
        if (savedInstanceState == null) {
            Log.d(TAG, "Creating new HomeFragment");
            showHomeFragment();
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                Log.d(TAG, "Home navigation selected");
                showHomeFragment();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Log.d(TAG, "Profile navigation selected");
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                currentFragment = selectedFragment;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void showHomeFragment() {
        HomeFragment homeFragment = new HomeFragment();
        currentFragment = homeFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, homeFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        Log.d(TAG, "User signing out");
        authManager.signOut(() -> startLoginActivity());
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume - refreshing current fragment");

        // Refresh current fragment data if it's HomeFragment
        refreshCurrentFragment();
    }

    private void refreshCurrentFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (fragment instanceof HomeFragment) {
            Log.d(TAG, "Refreshing HomeFragment");
            // HomeFragment will auto-refresh in its onResume()
        }
        // Add other fragments if needed
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "MainActivity onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        // Forward the result to the current fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == RESULT_OK) {
            Log.d(TAG, "Activity returned with success - refreshing current fragment");
            refreshCurrentFragment();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "MainActivity onNewIntent - refreshing data");
        refreshCurrentFragment();
    }

    @Override
    public void onBackPressed() {
        // Check if we're on home fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof HomeFragment) {
            // If on home fragment, minimize app instead of closing
            moveTaskToBack(true);
        } else {
            // If on other fragments, go back to home
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy");
        binding = null;
    }
}