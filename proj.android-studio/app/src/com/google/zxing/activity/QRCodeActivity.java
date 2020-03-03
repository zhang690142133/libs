package com.google.zxing.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.zxing.encoding.EncodingHandler;

import xsg.lychee.richalpha.utils.BitmapUtil;
import xsg.lychee.richalpha.utils.Constant;
import xsg.lychee.richalpha.utils.LocalizationUtil;

import xsg.lychee.richalpha.R;

public class QRCodeActivity extends AppCompatActivity {
    private ImageView imgQrcode;
    private ImageButton back;
    private Button scan;
    private Context _context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        _context = this;
        Intent intent_cur = getIntent();
        final String headUrl = intent_cur.getStringExtra(Constant.INTENT_EXTRA_KEY_QR_HEAD);
        final String description = intent_cur.getStringExtra(Constant.INTENT_EXTRA_KEY_QR_DESCRIPTION);

        back = (ImageButton) findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        scan = (Button) findViewById(R.id.btn_qrcodeTool);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QRCodeActivity.this, CaptureActivity.class);
                intent.putExtra(Constant.INTENT_EXTRA_KEY_QR_HEAD, headUrl);
                intent.putExtra(Constant.INTENT_EXTRA_KEY_QR_DESCRIPTION, description);
                startActivityForResult(intent, Constant.REQ_QR_CODE_CAPTURE);

                finish();
            }
        });

        imgQrcode = (ImageView) findViewById(R.id.img_qrcode);
        ViewTreeObserver vto = imgQrcode.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgQrcode.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.flash_on);

                try {
//            FileInputStream fis = new FileInputStream("/data/user/0/xsg.lychee.richalpha/files/chat/png/png_1561960813144_0.png");
//            FileInputStream fis = new FileInputStream("/storage/emulated/0/DCIM/Camera/P_20190627_164500_vHDR_On.jpg");
                    Bitmap logo = BitmapUtil.decodeUri(_context, Uri.parse("file://" + headUrl), 40, 40);
                    showQRCodeImage(description, logo);
                } catch (final Exception e) {
                    Log.d("FileInputStream", "onCreate: " + e.getMessage());
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalizationUtil.attachBaseContext(newBase));
    }

    public void showQRCodeImage(String codeString, Bitmap logo) {
        if (logo == null) {
            imgQrcode.setImageBitmap(EncodingHandler.createQRCode(codeString, imgQrcode.getWidth(), imgQrcode.getHeight()));
        }
        else {
            imgQrcode.setImageBitmap(EncodingHandler.createQRCode(codeString, imgQrcode.getWidth(), imgQrcode.getHeight(), logo));
        }
    }
}
