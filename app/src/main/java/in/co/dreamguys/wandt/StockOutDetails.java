package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import in.co.dreamguys.wandt.util.DatabaseHelper;
import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.ProgressDlg;
import in.co.dreamguys.wandt.adapter.StockOutDetailsAdapter;

public class StockOutDetails extends Activity implements View.OnClickListener {
    TextView title;
    ListView productList;
    FloatingActionButton fab;
    String stock_id, app_url,url = "api/get_product_details";

    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_STATUS = "status";

    private static final String TAG_ID = "prod_id";
    private static final String TAG_NAME = "prod_name";
    private static final String TAG_IMG = "prod_img";
    private static final String TAG_CODE = "prod_code";
    private static final String TAG_QTY = "prod_qty";
    private static final String TAG_AMT = "prod_amt";

    JSONArray products = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.stock_out_details);
        //delete stock details
        DatabaseHelper emptyStock = new DatabaseHelper(StockOutDetails.this);
        emptyStock.open();
        emptyStock.deleteStockDetails();
        emptyStock.close();

        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("STOCK DETAILS");

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stock_id = prefs.getString("stock_id", "");

        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;

        new StockOutDetailsTaskRunner().execute(app_url + "?stock_id=" + stock_id.trim());
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                Intent stOption = new Intent(StockOutDetails.this,StockOption.class);
                startActivity(stOption);
                break;
        }
    }

    class StockOutDetailsTaskRunner extends AsyncTask<String, String , String>{
        JSONObject json = null;
        Drawable[] img_hsy;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(StockOutDetails.this, "Loading", "Please wait...");
        }

        @Override
        protected String doInBackground(String... stock_url) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(stock_url[0]);
                if(json!=null){
                    // Getting JSON Array
                    try {
                        List<String> p_id = new ArrayList<String>();
                        List<String> p_name = new ArrayList<String>();
                        List<String> p_qty = new ArrayList<String>();
                        List<String> p_amt = new ArrayList<String>();

                        if (json.getString(TAG_SUCCESS).contains("y")) {
                            products = json.getJSONArray(TAG_PRODUCTS);
                            DatabaseHelper insertStock = new DatabaseHelper(StockOutDetails.this);
                            insertStock.open();
                            img_hsy = new Drawable[products.length()];
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject todayChildNode = null;
                                try {
                                    todayChildNode = products.getJSONObject(i);
                                    // Storing  JSON item in a Variable
                                    p_id.add(todayChildNode.getString(TAG_ID));
                                    p_name.add(todayChildNode.getString(TAG_NAME));
                                    //p_img.add(todayChildNode.getString(TAG_IMG));
                                    p_qty.add(todayChildNode.getString(TAG_QTY));

                                    p_amt.add(todayChildNode.getString(TAG_AMT));
                                    //insert stock details for scan
                                    String sd_prod_id = todayChildNode.getString(TAG_ID);
                                    String sd_prod_name = todayChildNode.getString(TAG_NAME);
                                    String sd_prod_img = todayChildNode.getString(TAG_IMG);
                                    String sd_prod_code = todayChildNode.getString(TAG_CODE);
                                    String sd_prod_qty = todayChildNode.getString(TAG_QTY);
                                    String sd_prod_amt = todayChildNode.getString(TAG_AMT);
                                    insertStock.insertStockDetails(stock_id,sd_prod_id,sd_prod_name,sd_prod_code,sd_prod_qty,sd_prod_amt,sd_prod_img);
                                    InputStream is = null;
                                    try {
                                        URL urlImagem = new URL(todayChildNode.getString(TAG_IMG));
                                        is = (InputStream) urlImagem.getContent();
                                    } catch (MalformedURLException e1) {
                                        Log.e("Product Img", e1.toString());
                                        e1.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Drawable dw = Drawable.createFromStream(is, "src");
                                    img_hsy[i] = dw;

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            insertStock.close();

                            //list to array conversion
                            String[] idArr = new String[p_id.size()];
                            idArr = p_id.toArray(idArr);
                            String[] nameArr = new String[p_name.size()];
                            nameArr = p_name.toArray(nameArr);
                            String[] qtyArr = new String[p_qty.size()];
                            qtyArr = p_qty.toArray(qtyArr);
                            String[] amtArr = new String[p_amt.size()];
                            amtArr = p_amt.toArray(amtArr);
                            final StockOutDetailsAdapter listProducts = new StockOutDetailsAdapter(StockOutDetails.this,idArr,nameArr,qtyArr,amtArr,img_hsy);
                            final String stStatus = json.getString(TAG_STATUS);
                            StockOutDetails.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (stStatus.contains("1")) {
                                        fab.setVisibility(View.INVISIBLE);
                                    }
                                    productList = (ListView) findViewById(R.id.productList);
                                    productList.setAdapter(listProducts);
                                }
                            });
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

            }catch (Exception e){
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
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent so = new Intent(StockOutDetails.this,MainStockItem.class);
        startActivity(so);
        finish();
    }
}
