package model;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Cloudinary MediaManager
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "drhpn7rda"); // Replace with your Cloudinary cloud name
        config.put("api_key", "369847147916174");       // Replace with your Cloudinary API key
        config.put("api_secret", "YcfJhQYVAYf2NfK-BBVi-7G1PGI"); // Replace with your Cloudinary API secret
        MediaManager.init(this, config);
    }
}
