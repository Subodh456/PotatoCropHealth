package com.example.potatocrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.potatocrop.ml.AutoModel1;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Homepage extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private ImageView selectedImageView;
    private Button predictButton, recomButton;
    private TextView resultTextView;
    private static final String[] CLASS_NAMES = {"Early Blight", "Late Blight", "Healthy"};


    private Bitmap img;
    private boolean hasGreenColor(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int countGreenPixels = 0;
        for (int pixel : pixels) {
            int r = Color.red(pixel);
            int g = Color.green(pixel);
            int b = Color.blue(pixel);

            // Check if the pixel has a green color
            if (g > r && g > b) {
                countGreenPixels++;
            }
        }

        // If more than 5% of the pixels have green color, assume the image is a leaf
        return (countGreenPixels * 100.0 / pixels.length) > 5.0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        ImageView cameraSelect = findViewById(R.id.cameraselect);
        ImageView gallerySelect = findViewById(R.id.galleryselect);
        selectedImageView = findViewById(R.id.selectedImageView);
        predictButton = findViewById(R.id.predictButton);
        resultTextView = findViewById(R.id.resultTextView);
        recomButton=findViewById(R.id.recommendationButton);
        recomButton.setVisibility(View.INVISIBLE);
        cameraSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the camera app to take a picture
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        gallerySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to pick an image
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recomButton.setVisibility(View.VISIBLE);
                if (img != null) {
                    img = Bitmap.createScaledBitmap(img, 256, 256, true);

                    // Check if the image is a leaf
                    boolean isLeaf = hasGreenColor(img);

                    if (isLeaf) {
                        // Run leaf classification model
                        try {
                            AutoModel1 model = AutoModel1.newInstance(getApplicationContext());

                            // Creates inputs for reference.
                            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);

                            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                            tensorImage.load(img);
                            ByteBuffer byteBuffer = tensorImage.getBuffer();
                            inputFeature0.loadBuffer(byteBuffer);

                            // Runs model inference and gets result.
                            AutoModel1.Outputs outputs = model.process(inputFeature0);
                            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                            // Releases model resources if no longer used.
                            model.close();
                            float[] scores = outputFeature0.getFloatArray();
                            int maxIndex = 0;
                            for (int i = 1; i < scores.length; i++) {
                                if (scores[i] > scores[maxIndex]) {
                                    maxIndex = i;
                                }
                            }
                            String predictedClassName = CLASS_NAMES[maxIndex];
                            float confidenceScore = scores[maxIndex] * 100.0f; // convert score to percentage
                            String resultText = String.format("Predicted class: %s,Confidence: %.2f%%", predictedClassName, confidenceScore);
                            resultTextView.setText(resultText);

                        } catch (IOException e) {
                            // TODO Handle the exception
                        }
                    } else {
                        // Show error message if image is not a leaf
                        Toast.makeText(getApplicationContext(), "Please select an image of a leaf.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please select an image first.", Toast.LENGTH_SHORT).show();

                }


            }
        });

        recomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (img != null) {
                    // Get the predicted class from the model
                    String predictedClassName = resultTextView.getText().toString().split(": ")[1].split(",")[0];

                    // Launch the recommendation activity with the predicted class as an extra
                    Intent intent = new Intent(Homepage.this, RecommendationActivity.class);
                    intent.putExtra("predictedClass", predictedClassName);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select an image first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Load the taken picture into selectedImageView
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                selectedImageView.setImageBitmap(imageBitmap);
                img=imageBitmap;
            } else if (requestCode == REQUEST_PICK_IMAGE) {
                // Load the picked image into selectedImageView
                Uri imageUri = data.getData();
                try{
                    Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                    selectedImageView.setImageURI(imageUri);
                    img=bitmap;
                }  catch (IOException e) {
                    e.printStackTrace();
                }
                selectedImageView.setImageURI(imageUri);
            }
        }
    }
}








