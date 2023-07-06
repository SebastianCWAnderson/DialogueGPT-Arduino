package com.group17.dialoguegpt;
 
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
 
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
 
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.json.JSONException;
import org.json.JSONObject;
 
import java.io.IOException;
import java.util.ArrayList;
 
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
 
public class MainActivity extends AppCompatActivity {
 
    private ToggleButton themeSwitch;
    private ImageButton speakBtn;
    private TextView textDisplay;
    private ProgressBar progressBar;
    private TextView textOutput;
    private ImageButton settingsButton;
    private BrokerConnection brokerConnection;
 
    private UserSettings settings;
    private static boolean mqttMode = false;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        settings = (UserSettings) getApplication();
 
        initWidgets();
        // Source: https://www.youtube.com/watch?v=-u63b5X2NqE (SharedPreferences tutorial)
        loadSharedPreferences();
        initSwitchListener();
 
 
        brokerConnection = new BrokerConnection(getApplicationContext());
        brokerConnection.connectToMqttBroker();
 
        progressBar.setVisibility(View.INVISIBLE);
 
        // allow scrolling TextViews in case text overflows
        textDisplay.setMovementMethod(new ScrollingMovementMethod());
        textOutput.setMovementMethod(new ScrollingMovementMethod());
 
        speakBtn.setOnClickListener(view -> {
            // https://developer.android.com/reference/android/speech/RecognizerIntent
            // create a messaging object to request any action from another app, in this case
            // the speech recognition from google. Starts an activity that will prompt the user
            // for speech and send it through a speech recognizer
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            try {
                // obtain the result
                startActivityIntent.launch(intent);
                textDisplay.setText("");
                textOutput.setText("");
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
 
        settingsButton.setOnClickListener(view -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });
 
    }
 
    private void initWidgets() {
        speakBtn = findViewById(R.id.speakBtn);
        textDisplay = findViewById(R.id.textDisplay);
        progressBar = findViewById(R.id.indeterminateBar);
        textOutput = findViewById(R.id.textOutput);
        themeSwitch = findViewById(R.id.toggleButton);
        settingsButton = findViewById(R.id.settingsButton);
    }
 
    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.SYS_THEME);
        String apiKey = sharedPreferences.getString(UserSettings.API_KEY, "");
        settings.setApiKey(apiKey);
        settings.setCustomTheme(theme);
        updateView();
    }
 
    private void initSwitchListener() {
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    settings.setCustomTheme(UserSettings.DARK_THEME);
                else
                    settings.setCustomTheme(UserSettings.LIGHT_THEME);
 
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_THEME, settings.getCustomTheme());
                editor.apply();
                updateView();
            }
        });
    }
 
    private void updateView() {
        switch (settings.getCustomTheme()) {
            case UserSettings.LIGHT_THEME:
                themeSwitch.setChecked(false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                setMqttMode(false);
                break;
            case UserSettings.DARK_THEME:
                themeSwitch.setChecked(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                setMqttMode(false);
                break;
            case UserSettings.MQTT_THEME:
                setMqttMode(true);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                setMqttMode(false);
                break;
        }
    }
 
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
 
                    if (result.getData() != null) {
                        ArrayList<String> text = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        // display the first index of the array, which will contain our value
                        textDisplay.setText(text.get(0));
 
                        progressBar.setVisibility(View.VISIBLE);
                        try {
                            chatGPT();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
 
                    }
                }
            });
 
    void chatGPT() throws IOException, JSONException {
 
        String API_URL = "https://api.openai.com/v1/chat/completions";
 
        OkHttpClient client = new OkHttpClient();

        String apiKey = settings.getApiKey();
        String prompt = textDisplay.getText().toString();
        //simple prompt engineering, so the actions no longer have to be said word for word
        String engineeredPrompt = "You are a helpful assistant and friend to the user. If the user's prompt seems to be sad in tone or is requesting a happy song, please respond with simply [happySong]. If the user is requesting a sad song, respond with simply [sadSong]. If the user prompt is related to the Legend of Zelda, respond with just [secretJingle]. Otherwise, respond to the user's prompt normally. User Prompt: ";
        MediaType mediaType = MediaType.parse("application/json");
        // Source: https://www.freeformatter.com/json-escape.html
        RequestBody body = RequestBody.create("{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\",\"content\": \"" + engineeredPrompt + prompt + "\"}],\"temperature\": 0.8}", mediaType);
 
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
 
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                textOutput.setText("An error has occurred.");
            }
 
            @Override
            public void onResponse(Call call, Response response) throws IOException {
 
                String jsonData = response.body().string();
                String tryResponse = "";
                try {
                    JSONObject obj = new JSONObject(jsonData);
                    // Working with JSON in Java is so much more complicated than JS
                    tryResponse = obj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                } catch (JSONException e) {
                    tryResponse = jsonData;
                    e.printStackTrace();
                }
 
                final String wioString = tryResponse;
                //Remove all words that are wrapped in []
                String[] outputArray = tryResponse.split("\\s+");
                StringBuilder temp = new StringBuilder();
                for (String i : outputArray) {
                    if (!(i.charAt(0) == '[' && i.charAt(i.length() - 1) == ']')) {
                        temp.append(i).append(" ");
                    }
                }
                final String myResponse = temp.toString();
 
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        textOutput.setVisibility(View.VISIBLE);
                        textOutput.setText(myResponse);
                        brokerConnection.getMqttClient().publish("DialogueGPT/Response", wioString, 0, null);
                    }
                });
 
            }
        });
    }
 
    public static boolean isMqttMode() {
        return mqttMode;
    }
 
    public static void setMqttMode(boolean val) {
        mqttMode = val;
    }
}