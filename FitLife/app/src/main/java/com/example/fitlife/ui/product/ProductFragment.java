package com.example.fitlife.ui.product;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.fitlife.HomeActivity;
import com.example.fitlife.R;
import com.example.fitlife.RegisterActivity;
import com.example.fitlife.SignInActivity;
import com.example.fitlife.databinding.FragmentProductBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import model.Product;
import model.User;


public class ProductFragment extends Fragment {

    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FragmentProductBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProductBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        // Initialize Cloudinary
//        Map<String, String> config = new HashMap<>();
//        config.put("cloud_name", "drhpn7rda"); // Replace with your Cloudinary cloud name
//        config.put("api_key", "369847147916174");       // Replace with your Cloudinary API key
//        config.put("api_secret", "YcfJhQYVAYf2NfK-BBVi-7G1PGI"); // Replace with your Cloudinary API secret
//        MediaManager.init(getContext(), config);


        Button selectImageButton = root.findViewById(R.id.button5);
        Button uploadButton = root.findViewById(R.id.button4);
        ImageView imageView = root.findViewById(R.id.imageView2);

        // Initialize Image Picker
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imageView.setImageURI(imageUri); // Show selected image
                    }
                });

        // Open Gallery on Button Click
        selectImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Upload Image Button
        uploadButton.setOnClickListener(view -> {
            if (imageUri != null) {
               // add product
                EditText title = root.findViewById(R.id.editTextText2);
                EditText description = root.findViewById(R.id.editTextTextMultiLine);
                EditText price = root.findViewById(R.id.editTextNumber);
                EditText qty = root.findViewById(R.id.editTextNumber2);

                if(title.getText().toString().isEmpty()){
                    Toast.makeText(requireContext(), "Please add title.", Toast.LENGTH_SHORT).show();
                } else if(description.getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please add description.", Toast.LENGTH_SHORT).show();
                } else if(price.getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please add price.", Toast.LENGTH_SHORT).show();
                } else if(qty.getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please add quantity.", Toast.LENGTH_SHORT).show();
                } else {
                    // Upload image to Cloudinary
                    MediaManager.get().upload(imageUri)
                            .option("public_id", "product_" + System.currentTimeMillis()) // Unique public ID
                            .callback(new UploadCallback() {
                                @Override
                                public void onStart(String requestId) {
                                    // Upload started
                                    Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {
                                    // Upload in progress
                                }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    // Upload successful, get image URL
                                    String imageUrl = (String) resultData.get("url");

                                    // Create Product object with image URL
                                    Product product = new Product(
                                            title.getText().toString(),
                                            description.getText().toString(),
                                            price.getText().toString(),
                                            qty.getText().toString(),
                                            imageUrl
                                    );

                                    // Save product to Firestore
                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    firestore.collection("product")
                                            .add(product)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(requireContext(), "Product added successfully.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getContext(), HomeActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(requireContext(), "Failed to add product.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    // Upload failed
                                    Toast.makeText(requireContext(), "Image upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) {
                                    // Upload rescheduled
                                }
                            })
                            .dispatch();
                }

            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}