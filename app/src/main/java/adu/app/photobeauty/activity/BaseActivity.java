package adu.app.photobeauty.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Random;

import adu.app.photobeauty.untils.AppConfigHelper;
import adu.app.photobeautystar.full.R;

/**
 * Created by Thomc on 19/02/2016.
 */
public class BaseActivity extends Activity {
    protected InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showAdWall(AppConfigHelper.getInt(this, AppConfigHelper.CONF_KEY_AD_WALL_PRIMARY_RATE, AppConfigHelper.CONF_DEF_VAL_AD_WALL_PRIMARY_RATE));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void loadAd() {
        if (AppConfigHelper.getBoolean(this, AppConfigHelper.CONF_KEY_AD_BANNER_PRIMARY_ENABLE, AppConfigHelper.CONF_DEF_VAL_AD_BANNER_PRIMARY_ENABLE)) {
            AdView mAdView = (AdView) findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    protected void showAdWall(int rate) {
        int random = new Random().nextInt(100);
        Log.d("", "showAdWall rate: " + rate + " random: " + random);
        if (random < rate) {
            showAdWall();
        }
    }

    protected void showAdWall() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_wall_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
            }

            public void onAdLoaded() {
                mInterstitialAd.show();
            }
        });
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

}
