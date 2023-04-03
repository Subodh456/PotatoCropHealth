package com.example.potatocrop;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RecommendationActivity extends AppCompatActivity {
    private TextView recomTV;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_recommendation);




        String predictedClass=getIntent().getStringExtra("predictedClass");
        String recommendations =getRecommendations(predictedClass);
        recomTV=findViewById(R.id.recoTV);
        recomTV.setText(recommendations);



    }
    private String getRecommendations(String predictedClass){
        switch (predictedClass) {
            case "Early Blight":
                return "Recommendations for Early Blight:\n\n" +
                        "- Remove infected leaves and fruits\n" +
                        "- Apply fungicides\n" +
                        "- Practice crop rotation";
            case "Late Blight":
                return "Recommendations for Late Blight:\n\n" +
                        "- Remove infected leaves and fruits\n" +
                        "- Apply fungicides\n" +
                        "- Practice crop rotation";
            case "Bacterial Spot":
                return "Recommendations for Bacterial Spot:\n\n" +
                        "- Remove infected leaves and fruits\n" +
                        "- Apply copper-based fungicides\n" +
                        "- Practice crop rotation";
            case "Healthy":
                return "No recommendations needed for Healthy crops.";
            default:
                return "Unknown disease, cannot provide recommendations.";
        }
    }
}
