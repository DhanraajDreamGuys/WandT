package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.ProgressDlg;

public class ApiSetup extends Activity implements View.OnClickListener{
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    EditText siteURL;
    Button apiBtn;
    String siteText,isURL ;
    private static final String TAG_SUCCESS = "success";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.api_setup);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isURL = prefs.getString("api_url","");
        if(!isURL.isEmpty()){
            Intent log_stock = new Intent(ApiSetup.this,LoginActivity.class);
            startActivity(log_stock);
        }
        //site url
        siteURL = (EditText) findViewById(R.id.siteURL);
        siteURL.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        apiBtn = (Button) findViewById(R.id.apiBtn);
        apiBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.apiBtn:

                siteText  = siteURL.getText().toString();
                Pattern p = Patterns.WEB_URL;
                Matcher m = p.matcher(siteText);

                if(siteText == null || siteText.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter site url", Toast.LENGTH_SHORT).show();
                }else if(!m.matches()){
                    Toast.makeText(getApplicationContext(), "Please enter valid url", Toast.LENGTH_SHORT).show();
                }else{
                    if (siteText.length() > 0 && siteText.charAt(siteText.length() - 1)=='/') {
                        siteText = siteText.substring(0, siteText.length()-1);
                    }
                    if (!siteText.startsWith("http://") && !siteText.startsWith("https://")){
                        siteText = "http://"+siteText;
                    }
                    new ApiTaskRunner().execute(siteText+"/api");
                }
                break;
        }
    }

    class ApiTaskRunner extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(ApiSetup.this, "Connecting", "Please wait...");
        }

        @Override
        protected String doInBackground(String... api_url) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(api_url[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            // Things to be done while execution of long running operation is in
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            ProgressDlg.dismissProgressDialog();
            if (json != null) {
                try {
                    if (json.getString(TAG_SUCCESS).contains("y")) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("api_url", siteText+"/");
                        editor.apply();
                        Intent stock_log = new Intent(ApiSetup.this, LoginActivity.class);
                        startActivity(stock_log);
                        finish();
                    }else{
                        ApiSetup.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Invalid url", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                ApiSetup.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error. Please try again later", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAffinity(this);
    }

}

