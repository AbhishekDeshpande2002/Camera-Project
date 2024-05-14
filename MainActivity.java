package com.example.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Uri> arrayList;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == RESULT_OK) {
                arrayList.clear();
                if (o.getData() != null && o.getData().getClipData() != null) {
                    int count = o.getData().getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = o.getData().getClipData().getItemAt(i).getUri();
                        arrayList.add(imageUri);
                    }
                    if (arrayList.size() >= 1) {
                        upload.setEnabled(true);
                        textView.setText(MessageFormat.format("{0} Images selected.", arrayList.size()));
                    }
                }
            }
        }
    });
    TextView textView;
    MaterialButton select, upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(MainActivity.this);

        arrayList = new ArrayList<>();

        select = findViewById(R.id.selectImages);
        upload = findViewById(R.id.uploadImages);
        textView = findViewById(R.id.selectedTv);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload.setText("Uploading images...");
                upload.setEnabled(false);
                uploadImages(new ArrayList<>());
            }
        });
    }

    private void uploadImages(ArrayList<String> imageUrlList) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(UUID.randomUUID().toString());
        Uri uri = arrayList.get(imageUrlList.size());
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String url = task.getResult().toString();
                        imageUrlList.add(url);

                        if(arrayList.size() == imageUrlList.size()) {
                            //Uploaded images url is stored in imagesUrl ArrayList.
                            Toast.makeText(MainActivity.this, "Images uploaded successfully!", Toast.LENGTH_SHORT).show();
                            upload.setText("Upload images");
                            upload.setEnabled(true);
                        }else {
                            uploadImages(imageUrlList);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
