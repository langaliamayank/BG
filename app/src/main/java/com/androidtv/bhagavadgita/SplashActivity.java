package com.androidtv.bhagavadgita;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends MasterActivity {
    protected int splashTime = 3000;
    private Thread splashTread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashCall();
    }

    private void splashCall() {
        final SplashActivity sPlashScreen = this;

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(splashTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent i = new Intent();
                    i.setClass(sPlashScreen,
                            DashboardActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        splashTread.start();
    }
}
