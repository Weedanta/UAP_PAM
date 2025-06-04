package com.example.todolist.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.todolist.R;
import com.example.todolist.databinding.FragmentProfileBinding;
import com.example.todolist.data.firebase.FirebaseAuthManager;
import com.example.todolist.ui.main.LoginActivity;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new FirebaseAuthManager(requireActivity());
        setupProfile();
        setupButtons();
    }

    private void setupProfile() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            binding.tvUserName.setText(user.getDisplayName());
            binding.tvUserEmail.setText(user.getEmail());

            // Load profile image with Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.ivProfileImage);
            } else {
                // Set default profile image
                binding.ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Set join date (you might want to get this from Firestore)
            String joinDate = SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG, Locale.getDefault())
                    .format(new Date());
            binding.tvJoinDate.setText("Member since " + joinDate);
        }
    }

    private void setupButtons() {
        binding.btnSignOut.setOnClickListener(v -> {
            authManager.signOut(() -> {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });

        binding.cardStats.setOnClickListener(v -> {
            // TODO: Show statistics or navigate to stats screen
        });

        binding.cardSettings.setOnClickListener(v -> {
            // TODO: Navigate to settings
        });

        binding.cardHelp.setOnClickListener(v -> {
            // TODO: Navigate to help/support
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}