package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.co.dreamguys.wandt.network.DownloadHelper;
import in.co.dreamguys.wandt.adapter.HomeAdapter;
import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.ProgressDlg;

public class SalesDetails extends Activity {
    TextView title;
    ListView salesList;
    String order_id, app_url,url = "api/get_sales_details";

    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";

    private static final String TAG_ID = "prod_id";
    private static final String TAG_NAME = "prod_name";
    private static final String TAG_IMG = "prod_img";
    private static final String TAG_QTY = "prod_qty";
    private static final String TAG_AMT = "prod_amt";
    private static final String TAG_CAT = "prod_cat";

    JSONArray products = null;
    File pathDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sales_details);

        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("SALES DETAILS");
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        order_id = prefs.getString("sales_order_id", "");
        pathDirectory = new File(prefs.getString("pathDirectory", ""));
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;
        new SalesTaskRunner().execute(app_url + "?order_id=" + order_id.trim());
    }


    class SalesTaskRunner extends AsyncTask<String, String , String> {
        JSONObject json = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(SalesDetails.this, "Loading", "Please wait...");
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
                        List<String> p_img = new ArrayList<String>();
                        List<String> p_qty = new ArrayList<String>();
                        List<String> p_amt = new ArrayList<String>();
                        List<String> p_cat = new ArrayList<String>();

                        if (json.getString(TAG_SUCCESS).contains("y")) {
                            products = json.getJSONArray(TAG_PRODUCTS);
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject salesChildNode = null;
                                try {
                                    DownloadHelper dHelper = new DownloadHelper();
                                    salesChildNode = products.getJSONObject(i);
                                    String img_url = salesChildNode.getString(TAG_IMG);
                                    String fileName = img_url.substring(img_url.lastIndexOf('/') + 1, img_url.length());
                                    File myFile = new File(pathDirectory+"/"+fileName);
                                    String sd_prod_img = null;
                                    if(!myFile.exists()){
                                        //sd_prod_img = dHelper.getImageFromUrl(pathDirectory, fileName, img_url);
                                    }else{
                                        //sd_prod_img = pathDirectory+"/"+fileName;
                                    }
                                    p_id.add(salesChildNode.getString(TAG_ID));
                                    p_name.add(salesChildNode.getString(TAG_NAME));
                                    p_img.add(img_url);
                                    p_qty.add(salesChildNode.getString(TAG_QTY));
                                    p_amt.add(salesChildNode.getString(TAG_AMT));
                                    p_cat.add(salesChildNode.getString(TAG_CAT));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            //list to array conversion
                            String[] idArr = new String[p_id.size()];
                            idArr = p_id.toArray(idArr);
                            String[] nameArr = new String[p_name.size()];
                            nameArr = p_name.toArray(nameArr);
                            String[] imgArr = new String[p_img.size()];
                            imgArr = p_img.toArray(imgArr);
                            String[] qtyArr = new String[p_qty.size()];
                            qtyArr = p_qty.toArray(qtyArr);
                            String[] amtArr = new String[p_amt.size()];
                            amtArr = p_amt.toArray(amtArr);
                            String[] catArr = new String[p_cat.size()];
                            catArr = p_cat.toArray(catArr);

                            final HomeAdapter listProducts = new HomeAdapter(SalesDetails.this,idArr,nameArr,qtyArr,amtArr,imgArr,catArr);
                            SalesDetails.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    salesList = (ListView) findViewById(R.id.salesList);
                                    salesList.setAdapter(listProducts);
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
}
