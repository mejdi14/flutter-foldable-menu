package com.example.native_communication

import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {

    private static final String CHANNEL = "samples.flutter.io/battery";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this);

        // Direct new MethodChannel, then set a Callback to handle the Flutter call
        new MethodChannel(getFlutterView(), CHANNEL).setMethodCallHandler(
        new MethodCallHandler() {
            @Override
            public void onMethodCall(MethodCall call, Result result) {
                // handle the call from Flutter in this callback
            }
        });


        private fun getBatteryLevel() : Int{
        var batteryLevel = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var batteryManager =  getSystemService(BATTERY_SERVICE) as BatteryManager;
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            var intent = ContextWrapper(getApplicationContext()).
            registerReceiver(null,  IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            batteryLevel = ((intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)) )!!  /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }

        return batteryLevel;
    }

}
