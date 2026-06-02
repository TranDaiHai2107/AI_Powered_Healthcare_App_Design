package com.healthcare.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.healthcare.app.R;
import com.healthcare.app.databinding.ActivityRecordDetailBinding;
import com.healthcare.app.model.MedicalRecord;

import java.util.ArrayList;

public class RecordDetailActivity extends AppCompatActivity {

    private ActivityRecordDetailBinding binding;
    private FirebaseFirestore db;
    private String recordId;
    private MedicalRecord currentRecord;

    private final ActivityResultLauncher<String[]> getContentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    uploadToFirebaseStorage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecordDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        recordId = getIntent().getStringExtra("recordId");

        if (recordId == null) {
            Toast.makeText(this, "Invalid record", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnUpload.setOnClickListener(v -> {
            String[] mimeTypes = {"image/*", "application/pdf"};
            getContentLauncher.launch(mimeTypes);
        });

        loadRecord();
    }

    private void loadRecord() {
        db.collection("medical_records").document(recordId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentRecord = doc.toObject(MedicalRecord.class);
                        if (currentRecord != null) populateUI();
                    } else {
                        Toast.makeText(this, "Record not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load record", Toast.LENGTH_SHORT).show());
    }

    private void populateUI() {
        binding.tvTitle.setText(currentRecord.getTitle() != null ? currentRecord.getTitle() : "—");
        binding.tvDoctorHospital.setText((currentRecord.getDoctor() != null ? currentRecord.getDoctor() : "—") + " — " +
                (currentRecord.getHospital() != null ? currentRecord.getHospital() : "—"));
        binding.tvDate.setText(currentRecord.getDate() != null ? currentRecord.getDate() : "—");

        binding.tvDiagnosis.setText(currentRecord.getDiagnosis() != null ? currentRecord.getDiagnosis() : "No diagnosis available");
        binding.tvPrescription.setText(currentRecord.getPrescription() != null ? currentRecord.getPrescription() : "No prescription available");

        binding.layoutAttachments.removeAllViews();
        if (currentRecord.getAttachmentUrls() != null) {
            for (int i = 0; i < currentRecord.getAttachmentUrls().size(); i++) {
                String url = currentRecord.getAttachmentUrls().get(i);
                TextView tv = new TextView(this);
                tv.setText("Attachment " + (i + 1));
                tv.setTextColor(getResources().getColor(R.color.pastel_blue_dark, null));
                tv.setPadding(0, 8, 0, 8);
                tv.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                });
                binding.layoutAttachments.addView(tv);
            }
        }
    }

    private void uploadToFirebaseStorage(Uri fileUri) {
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        binding.btnUpload.setEnabled(false);

        String fileName = "attachment_" + System.currentTimeMillis();
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("attachments/" + recordId + "/" + fileName);

        ref.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    db.collection("medical_records").document(recordId)
                            .update("attachmentUrls", FieldValue.arrayUnion(downloadUrl))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                                binding.btnUpload.setEnabled(true);
                                loadRecord(); // Reload to show new attachment
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update record", Toast.LENGTH_SHORT).show();
                                binding.btnUpload.setEnabled(true);
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    binding.btnUpload.setEnabled(true);
                });
    }
}
