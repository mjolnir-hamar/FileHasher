package com.mjolnir_hamar.filehasher;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private String chosenDigest;
    private Uri fileUri;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView t = findViewById(R.id.header_text);
        t.setText("File Hash Calculator");

        Button filePicker = findViewById(R.id.file_picker_button);
        filePicker.setText("Pick file");
        Button calculate = findViewById(R.id.calculate_button);
        calculate.setText("Waiting for file and algorithm selection...");

        TextView fileName = findViewById(R.id.file_name);
        fileName.setText("");

        TextView digestResult = findViewById(R.id.digest);
        digestResult.setText("");

        RadioGroup digests = findViewById(R.id.digests);

        ActivityResultLauncher<Intent> fileLauncher;
        fileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                o -> {
                    if (o.getResultCode() == RESULT_OK && o.getData() != null) {
                        Intent data = o.getData();
                        if (data.getData() != null) {
                            this.fileUri = data.getData();
                            if (this.chosenDigest != null) {
                                calculate.setText("Calculate!");
                            } else {
                                calculate.setText("Waiting for algorithm selection...");
                            }
                            fileName.setText(this.fileUri.getPath());
                        }
                    }
                }
        );

        filePicker.setOnClickListener(view -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*");
            fileLauncher.launch(chooseFile);
        });

        calculate.setOnClickListener(view -> {
            try {
                InputStream f = getContentResolver().openInputStream(fileUri);
                Log.d("digest", chosenDigest);
                MessageDigest digest = MessageDigest.getInstance(chosenDigest);
                try (DigestInputStream digestInputStream = new DigestInputStream(f, digest)) {
                    while (digestInputStream.read() != -1) {}
                }
                byte[] checksum = digest.digest();

                BigInteger bi = new BigInteger(1, checksum);

                f.close();
                digestResult.setText(String.format("%0" + (checksum.length << 1) + "X", bi));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                Log.d("MD", "No " + chosenDigest);
                throw new RuntimeException(e);
            }
        });

        digests.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            chosenDigest = rb.getText().toString();
            if (this.fileUri != null) {
                calculate.setText("Calculate!");
            } else {
                calculate.setText("Waiting for file selection...");
            }
        });
    }
}