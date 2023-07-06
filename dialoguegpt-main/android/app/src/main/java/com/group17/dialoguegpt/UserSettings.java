package com.group17.dialoguegpt;

import android.app.Application;

public class UserSettings extends Application {

    public static final String PREFERENCES = "preferences";
    public static final String CUSTOM_THEME = "customTheme";
    public static final String LIGHT_THEME = "lightTheme";
    public static final String DARK_THEME = "darkTheme";
    public static final String SYS_THEME = "sysTheme";
    public static final String MQTT_THEME = "mqttTheme";
    public static final String API_KEY = "apiKey";

    private String customTheme;

    private String apiKey;

    public String getCustomTheme() {
        return customTheme;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setCustomTheme(String customTheme) {
        this.customTheme = customTheme;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
