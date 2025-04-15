package com.outsystems.plugins.barcodescanner;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeCallback;

public class BarcodeScannerView extends FrameLayout {

    private DecoratedBarcodeView barcodeView;

    public BarcodeScannerView(Context context) {
        super(context);
        init(context);
    }

    public BarcodeScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        barcodeView = new DecoratedBarcodeView(context);
        barcodeView.getBarcodeView().resume();
        this.addView(barcodeView);
    }

    public void startDecoding() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    OSBarcodeScanner.sendScanResult(result.getText()); // Envia el resultado al plugin
                    stopDecoding(); // Detiene despues del primer resultado
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Se puede dejar vacio si no se quiere trabajar con los puntos de enfoque
            }
        });
    }

    public void stopDecoding() {
        barcodeView.pause();
    }
}
