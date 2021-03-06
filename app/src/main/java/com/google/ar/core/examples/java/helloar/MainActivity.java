/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.helloar;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.examples.java.helloar.arcoremanager.ArCoreManager;
import com.google.ar.core.examples.java.helloar.arcoremanager.object.BugDroidArCoreObjectDrawer;
import com.google.ar.core.examples.java.helloar.settings.LinesSettings;
import com.google.ar.core.examples.java.helloar.settings.ObjectSettings;
import com.google.ar.core.examples.java.helloar.settings.SettingsView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using
 * the ARCore API. The application will display any detected planes and will allow the user to
 * tap on a plane to place a 3d model of the Android robot.
 */
public class MainActivity extends AppCompatActivity {

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    @BindView(R.id.surfaceview)
    GLSurfaceView mSurfaceView;

    private ArCoreManager arCoreManager;

    @BindView(R.id.bottomNav)
    BottomNavigationView bottomNavigationView;

    @BindView(R.id.message)
    TextView message;

    @BindView(R.id.configLocal)
    ViewGroup configLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    public void init(){
        if(arCoreManager != null){
            return;
        }
        arCoreManager = new ArCoreManager(this, new ArCoreManager.Listener() {
            @Override
            public void onArCoreUnsuported() {
                Toast.makeText(MainActivity.this, "This device does not support AR", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onPermissionNotAllowed() {
                //on permission not allowed
                Toast.makeText(MainActivity.this,
                        "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void showLoadingMessage() {
                runOnUiThread(() -> {
                    message.setText("Searching for surfaces...");
                    message.animate().alpha(1f);
                });
            }

            @Override
            public void hideLoadingMessage() {
                runOnUiThread(() -> {
                    message.animate().alpha(0f);
                });
            }
        });

        arCoreManager.setup(mSurfaceView);

        arCoreManager.addObjectToDraw(new BugDroidArCoreObjectDrawer());

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.settings:
                            openSettings();
                            //Action quand onglet 1 sélectionné
                            return false;
                        case R.id.onTap_addAndroidObject:
                            configLocal.removeAllViews();
                            configLocal.addView(new ObjectSettings(this, objectTouchMode -> arCoreManager.setTouchMode(objectTouchMode)));
                            arCoreManager.setCaptureLines(false);
                            return true;
                        case R.id.onTouch_addLines:
                            configLocal.removeAllViews();
                            configLocal.addView(new LinesSettings(this, arCoreManager.getSettings()));
                            arCoreManager.setCaptureLines(true);
                            return true;
                        default:
                            //Action quand onglet 3 sélectionné
                            return false;
                    }
                }
        );

        bottomNavigationView.setSelectedItemId(R.id.onTap_addAndroidObject);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!PermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this,
                    "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (PermissionHelper.hasCameraPermission(this)) {
            // Note that order matters - see the note in onPause(), the reverse applies here.
            init();
        } else {
            PermissionHelper.requestCameraPermission(this);
        }
    }

    private void openSettings() {
        new AlertDialog.Builder(this)
                .setView(new SettingsView(this, arCoreManager.getSettings()))
                .create()
                .show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
