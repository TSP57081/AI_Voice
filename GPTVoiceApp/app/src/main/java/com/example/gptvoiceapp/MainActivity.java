package com.example.gptvoiceapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private SingletonClass singletonClass;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.background_dark));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background_dark));

        singletonClass = SingletonClass.getInstance();
        RelativeLayout micIcon = findViewById(R.id.micIcon);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextViewAdapter adapter = new TextViewAdapter();
        recyclerView.setAdapter(adapter);
        singletonClass.setAdapter(adapter);
        singletonClass.setChatList(new ArrayList<>());
        tts = new TextToSpeech(this, this);

        GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash", "AIzaSyAl0CCChc0nthYrppvDJvyj_7UlxGNOrJA");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Request microphone permission
        requestMicrophonePermission();

        micIcon.setOnClickListener(v -> startSpeechToText());
    }

    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void startSpeechToText() {
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "");

        try {
            startActivityForResult(speechIntent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),
                    "Speech-to-text not supported on your device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    ArrayList<String> chatList = singletonClass.getChatList();
                    if (chatList == null) {
                        chatList = new ArrayList<>();
                    }
                    chatList.add(spokenText.trim());
                    singletonClass.setChatList(chatList);

                    makeAPICallToGPT(spokenText);
                }
            }
        }
    }

    private void makeAPICallToGPT(String spokenText) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyAl0CCChc0nthYrppvDJvyj_7UlxGNOrJA");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(spokenText + ". Keep the answer short.")
                .build();

        Executor executor = Executors.newCachedThreadPool();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                runOnUiThread(() -> {
                    ArrayList<String> al = singletonClass.getChatList();
                    assert resultText != null;
                    al.add(resultText.trim());
                    singletonClass.setChatList(al);
                });
                speakOut(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, executor);
    }

    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with speech recognition
            } else {
                Log.e("","Permission denied!");
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TAG", "Text-to-speech language is not supported.");
            }
        } else {
            Log.e("TAG", "Text-to-speech initialization failed.");
        }
    }
}