package com.outsystems.plugins.barcodescanner;
import android.content.Intent;
import android.hardware.Camera;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


// Para tener una vista embebida
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.Gravity;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import android.app.Activity;
// Para tener una vista embebida


public class OSBarcodeScanner extends CordovaPlugin {
    
    private BarcodeScannerView scannerView;

    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;
    static private CallbackContext _callbackContext;

    public static final int BACK_CAMERA = 1;
    public static final int FRONT_CAMERA = 2;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        _callbackContext = callbackContext;

        if (action.equals("scan")) {

            JSONObject params = args.optJSONObject(0);
            String scanInstructions = params.optString("scan_instructions");
            int cameraDirection = params.optInt("camera_direction");
            int scanOrientation = params.optInt("scan_orientation");
            boolean scanLine = params.optBoolean("scan_line");
            boolean scanButton = params.optBoolean("scan_button");
            String scanText = params.optString("scan_button_text");

            this.scan(scanInstructions,
                    cameraDirection,
                    scanOrientation,
                    scanLine,
                    scanButton,
                    scanText);
            return true;
        }
        return false;
    }

    private void scan(String scanInstructions,
                  int cameraDirection,
                  int scanOrientation,
                  boolean scanLine,
                  boolean scanButton,
                  String scanText) {

        Activity activity = cordova.getActivity();

        activity.runOnUiThread(() -> {
            if (scannerView != null) {
                return; // Ya esta escaneando
            }

            scannerView = new BarcodeScannerView(activity);

            // Puedes ajustar el tamano aqui
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT // o 500dp si lo quieres parcial
            );
            params.gravity = Gravity.TOP;

            // Inserta la vista en el layout actual
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.addView(scannerView, params);

            scannerView.startDecoding();

            scannerView.decode(result -> {
                if (result != null && result.getText() != null) {
                    rootView.removeView(scannerView);
                    scannerView = null;

                    if (_callbackContext != null) {
                        _callbackContext.success(result.getText());
                    }
                }
            });
        });
    }

    // private void scan(String scanInstructions,
    //                   int cameraDirection,
    //                   int scanOrientation,
    //                   boolean scanLine,
    //                   boolean scanButton,
    //                   String scanText) {

    //     IntentIntegrator integrator = new IntentIntegrator(this.cordova.getActivity());
    //     integrator.setOrientationLocked(false);

    //     if (cameraDirection == BACK_CAMERA) {
    //         integrator.setCameraId(0);
    //     } else if (cameraDirection == FRONT_CAMERA) {
    //         integrator.setCameraId(1);
    //     }

    //     integrator.setCaptureActivity(CustomScannerActivity.class);
    //     integrator.addExtra("SCAN_INSTRUCTIONS", scanInstructions);
    //     integrator.addExtra("SCAN_ORIENTATION", scanOrientation);
    //     integrator.addExtra("SCAN_LINE", scanLine);
    //     integrator.addExtra("SCAN_BUTTON", scanButton);
    //     integrator.addExtra("SCAN_TEXT", scanText);
    //     integrator.initiateScan();

    //     this.cordova.setActivityResultCallback(this);
    // }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (_callbackContext != null) {
            if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
                // This is important, otherwise the result will not be passed to the fragment
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
    
            IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

            if(result.getContents() == null) {
                Intent originalIntent = result.getOriginalIntent();

                try {
                    JSONObject jsonResult = new JSONObject();
                    if (originalIntent == null) {
                        jsonResult.put("code", "OS-PLUG-BARC-0006");
                        jsonResult.put("message", "Scanning cancelled.");
                    } else if(originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                        jsonResult.put("code", "OS-PLUG-BARC-0007");
                        jsonResult.put("message", "Scanning cancelled due to missing camera permissions.");
                    }
                    _callbackContext.error(jsonResult);

                } catch (Exception e) {
                    _callbackContext.error("Cancelled");
                }

            } else {
                _callbackContext.success(result.getContents());
            }
        }
    }

    public static void sendScanResult(String text) {
        if (_callbackContext != null) {
            _callbackContext.success(text);
        }
    }
    
}