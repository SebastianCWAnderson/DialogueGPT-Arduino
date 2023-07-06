package com.group17.dialoguegpt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import timber.log.Timber;

public class SettingsActivity extends AppCompatActivity {

    private Button submitKeyButton;
    private TextInputEditText apiKeyField;
    private Button closeSettingsButton;
    private Spinner spinner;
    private UserSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_window);

        settings = (UserSettings) getApplication();

        initWidgets();
        initSpinner();
        loadSharedPreferences();
        initSpinnerListener();

        apiKeyField.setText(settings.getApiKey());

        submitKeyButton.setOnClickListener(view -> {
            settings.setApiKey(Objects.requireNonNull(apiKeyField.getText()).toString());
            SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
            editor.putString(UserSettings.API_KEY, settings.getApiKey());
            editor.apply();
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Timber.tag("Keyboard not opened").e(e.toString());
            }
            Toast.makeText(this, "API key saved successfully", Toast.LENGTH_SHORT).show();
            apiKeyField.setText(settings.getApiKey());
        });


        closeSettingsButton.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void initWidgets() {
        submitKeyButton = findViewById(R.id.submitKeyButton);
        apiKeyField = findViewById(R.id.apiKeyField);
        closeSettingsButton = findViewById(R.id.closeSettingsButton);
        spinner = findViewById(R.id.spinner);
    }

    private void initSpinner() {
        Resources resources = getResources();
        String[] lightModes = resources.getStringArray(R.array.lightModes);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.dropdown_item, lightModes);
        arrayAdapter.setDropDownViewResource(R.layout.dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE);
        String theme = sharedPreferences.getString(UserSettings.CUSTOM_THEME, UserSettings.SYS_THEME);
        String apiKey = sharedPreferences.getString(UserSettings.API_KEY, "");
        settings.setApiKey(apiKey);
        settings.setCustomTheme(theme);
        updateView();
    }

    private void initSpinnerListener() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = spinner.getSelectedItem().toString();
                switch (item) {
                    case "Light":
                        settings.setCustomTheme(UserSettings.LIGHT_THEME);
                        break;
                    case "Dark":
                        settings.setCustomTheme(UserSettings.DARK_THEME);
                        break;
                    case "MQTT":
                        settings.setCustomTheme(UserSettings.MQTT_THEME);
                        break;
                    default:
                        settings.setCustomTheme(UserSettings.SYS_THEME);
                        break;
                }
                SharedPreferences.Editor editor = getSharedPreferences(UserSettings.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(UserSettings.CUSTOM_THEME, settings.getCustomTheme());
                editor.apply();
                updateView();
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateView() {
        switch (settings.getCustomTheme()) {
            case UserSettings.LIGHT_THEME:
                spinner.setSelection(1);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case UserSettings.DARK_THEME:
                spinner.setSelection(2);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case UserSettings.MQTT_THEME:
                spinner.setSelection(3);
                // broker MQTT magic TODO
                break;
            default:
                spinner.setSelection(0);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;

        }
    }
}