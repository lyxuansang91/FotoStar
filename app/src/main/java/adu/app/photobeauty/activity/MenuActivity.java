package adu.app.photobeauty.activity;

import java.io.File;

import adu.app.photobeauty.untils.AppConfigHelper;
import adu.app.photobeauty.untils.Constant;
import adu.app.photobeauty.untils.Utils;
import adu.app.photobeautystar.full.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends BaseActivity implements OnClickListener {
    private final static int ACTIVITY_PICK_IMAGE = 0;
    private final static int ACTIVITY_TAKE_PHOTO = 1;

    private Button galleryButton;
    private Button cameraButton;
    private Button collageButton;
    private TextView titleTV;
    private Dialog pickPopup;
    private long lastBackPressed;
    private Spinner languageSpinner;
    private final static String[] languageValues = new String[2];
    private boolean init = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Constant.checkAlive(this))
            return;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu);
        galleryButton = (Button) findViewById(R.id.choose_from_sd_button);
        galleryButton.setOnClickListener(this);
        cameraButton = (Button) findViewById(R.id.take_picture_button);
        cameraButton.setOnClickListener(this);
        collageButton = (Button) findViewById(R.id.collage_button);
        collageButton.setOnClickListener(this);
        titleTV = (TextView) findViewById(R.id.tvTitleText);
        titleTV.setText(getString(R.string.app_name));
        findViewById(R.id.backTitleGroup).setVisibility(View.GONE);
        findViewById(R.id.checkTitleGroup).setVisibility(View.GONE);
        findViewById(R.id.menuBtn).setVisibility(View.VISIBLE);
        languageSpinner = (Spinner) findViewById(R.id.languageList);
        languageValues[0] = getString(R.string.lbl_automatic);
        // languageValues[0] = Locale.getDefault().getDisplayLanguage();
        languageValues[1] = getString(R.string.lbl_english);
        // if (!Utils.isSupportCurrentLang(this))
        // languageValues[0] = getString(R.string.lbl_automatic);
        String saveLang = Utils.getValueByKey(this, Constant.LANGUAGE_PREF_KEY);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.list_size, languageValues);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(dataAdapter);
        if (saveLang.equals(Constant.LANGUAGE_EN_VAL))
            languageSpinner.setSelection(1);
        else
            languageSpinner.setSelection(0);
        languageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterview, View view,
                                       int i, long l) {
                // TODO Auto-generated method stub
                Log.v("", "onItemSelected: " + i);
                updateLanguage(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterview) {
                // TODO Auto-generated method stub

            }
        });
        Constant.init(this);
        Utils.initImageLoader(this);
        // Config.updateConfig(this, "ca-app-pub-3336718604400772/5914033649",
        // "ca-app-pub-3336718604400772/1564559243");
        // Config.maybeShowAdWall(this);
        AppConfigHelper.update(this);
    }

    protected void updateLanguage(int i) {
        // TODO Auto-generated method stub

        if (i == 0) {
            Utils.setValueByKey(Constant.LANGUAGE_AUTO_VAL, this,
                    Constant.LANGUAGE_PREF_KEY);

        } else {
            Utils.setValueByKey(Constant.LANGUAGE_EN_VAL, this,
                    Constant.LANGUAGE_PREF_KEY);
        }
        Utils.setLocale(this);
        if (!init) {
            finish();
            startActivity(new Intent(this, MenuActivity.class));
        }
        init = false;

    }

    public void onResume() {
        super.onResume();
        loadAd();
        // Config.onResumeService(this);
        // Config.showAdBanner(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Config.onPauseService(this);

    }

    public void onMenuBtn(View v) {
        showMenu();
    }

    public void showMenu() {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_menu, null);
        Button likeBtn = (Button) layout.findViewById(R.id.likebtn);
        TextView likeTV = (TextView) layout.findViewById(R.id.liketv);
        likeBtn.setOnClickListener(likeListener);
        likeTV.setOnClickListener(likeListener);
        Button shareBtn = (Button) layout.findViewById(R.id.sharebtn);
        TextView shareTV = (TextView) layout.findViewById(R.id.sharetv);
        shareBtn.setOnClickListener(shareListener);
        shareTV.setOnClickListener(shareListener);
        Button contactBtn = (Button) layout.findViewById(R.id.contactbtn);
        TextView contactTV = (TextView) layout.findViewById(R.id.contacttv);
        contactBtn.setOnClickListener(contactListener);
        contactTV.setOnClickListener(contactListener);
        pickPopup = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar);

        pickPopup.setContentView(layout);
        pickPopup.setCanceledOnTouchOutside(true);
        pickPopup.setCancelable(true);
        pickPopup.show();
    }

    private OnClickListener shareListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            pickPopup.dismiss();
            Utils.shareIt(MenuActivity.this,
                    MenuActivity.this.getString(R.string.tvshareapp),
                    Constant.APP_LINK);
        }
    };

    private OnClickListener contactListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            pickPopup.dismiss();
            String title = MenuActivity.this.getString(R.string.tvcontact);
            Intent emailIntent = new Intent(
                    Intent.ACTION_SENDTO,
                    Uri.fromParts(
                            "mailto",
                            MenuActivity.this.getString(R.string.contact_email),
                            null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hi");
            MenuActivity.this.startActivity(Intent.createChooser(emailIntent,
                    title));
        }
    };
    private OnClickListener likeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            pickPopup.dismiss();
            showAppOnMarket();
        }
    };

    public void showAppOnMarket() {
        try {
            Intent localIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse(Constant.APP_LINK_MARKET));
            MenuActivity.this.startActivity(localIntent);
        } catch (Exception e) {
            // TODO: handle exception
            Intent localIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse(Constant.APP_LINK));
            MenuActivity.this.startActivity(localIntent);

        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_from_sd_button:
                openGalleryButtonClicked();
                break;
            case R.id.take_picture_button:
                takePictureButtonClicked();
                break;
            case R.id.collage_button:
                Intent intent = new Intent(this, GalleryActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void openGalleryButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ACTIVITY_PICK_IMAGE);
    }

    private void takePictureButtonClicked() {
        Uri imageUri = Uri.fromFile(getTempFile(getApplicationContext()));
        Intent intent = createIntentForCamera(imageUri);
        startActivityForResult(intent, ACTIVITY_TAKE_PHOTO);
    }

    private File getTempFile(Context context) {
        String fileName = "camera.tmp";
        File path = Utils.createAppFolder();
        if (!path.exists()) {
            path.mkdir();
        }
        return new File(path, fileName);
    }

    private Intent createIntentForCamera(Uri imageUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_PICK_IMAGE:
                    startEditorActivity(data.getData().toString());
                    break;
                case ACTIVITY_TAKE_PHOTO:
                    // taken photo is in sent url, data is null
                    Uri uri = Uri.fromFile(getTempFile(getApplicationContext()));
                    startEditorActivity(uri.toString());
                    break;
                default:
                    break;
            }
        }
    }

    private void startEditorActivity(String url) {
        // Intent i = new Intent(this, TestCommandActivity.class);
        Intent i = new Intent(this, PhotoEditActivity.class);
        i.putExtra("path", url);
        startActivity(i);
    }

    public void showRateDialog() {

        View checkBoxView = View.inflate(this, R.layout.checkbox, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                Log.v("", "checkbox: " + arg1);
                Utils.setValueByKey(String.valueOf(arg1), MenuActivity.this,
                        Constant.RATE_PREF_KEY);

            }
        });
        checkBox.setText(getString(R.string.msg_rate_checkbox));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_rate))
                .setView(checkBoxView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.lbl_rate_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showAppOnMarket();
                            }
                        })
                .setNegativeButton(getString(R.string.lbl_rate_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                MenuActivity.super.onBackPressed();
                            }
                        }).show();
    }

    public void onBackPressed() {

        if (!Boolean
                .valueOf(Utils.getValueByKey(this, Constant.RATE_PREF_KEY))) {
            showRateDialog();
            return;
        }
        if (System.currentTimeMillis() - lastBackPressed > Constant.deltaBackPress) {
            Toast.makeText(this, getString(R.string.msg_back_press),
                    Toast.LENGTH_SHORT).show();
            lastBackPressed = System.currentTimeMillis();
            return;
        }
        super.onBackPressed();

    }

    public void onStop() {
        super.onStop();
        // Config.hideAdView();

    }
}
