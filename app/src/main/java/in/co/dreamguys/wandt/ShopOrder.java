package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.ProgressDlg;
import in.co.dreamguys.wandt.adapter.StoreAdapter;


public class ShopOrder extends Activity {
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    ListView orderList;
    String driver_id, app_url, url = "api/store_orders";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_STORE = "data";
    private static final String TAG_NO = "id";
    private static final String TAG_BILL = "billing";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_AMT = "amount";
    JSONArray orders = null;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.shop_orders);
        orderList = (ListView)findViewById(R.id.orderList);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        driver_id = prefs.getString("driver_id", "");
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("Shop Order");
        new OrderSync().execute(app_url+"?driver_id="+driver_id.trim());
    }

    class OrderSync extends AsyncTask<String, String , String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(ShopOrder.this, "Loading", "Please wait...");
        }
        @Override
        protected String doInBackground(String... store_url) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(store_url[0]);
                if (json != null) {
                    // Getting JSON Array
                    try {
                        List<String> st_id = new ArrayList<String>();
                        List<String> st_biller = new ArrayList<String>();
                        List<String> st_item = new ArrayList<String>();
                        List<String> st_amt = new ArrayList<String>();
                        if (json.getString(TAG_SUCCESS).contains("y")) {
                            orders = json.getJSONArray(TAG_STORE);
                            for (int i = 0; i < orders.length(); i++) {
                                JSONObject storeChildNode = null;
                                try {
                                    storeChildNode = orders.getJSONObject(i);
                                    st_id.add(storeChildNode.getString(TAG_NO));
                                    st_biller.add(storeChildNode.getString(TAG_BILL));
                                    st_item.add(storeChildNode.getString(TAG_ITEMS));
                                    st_amt.add(storeChildNode.getString(TAG_AMT));
                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            //list to array conversion
                            String[] idArr = new String[st_id.size()];
                            idArr = st_id.toArray(idArr);
                            String[] billArr = new String[st_biller.size()];
                            billArr = st_biller.toArray(billArr);
                            String[] itemArr = new String[st_item.size()];
                            itemArr = st_item.toArray(itemArr);
                            String[] amtArr = new String[st_amt.size()];
                            amtArr = st_amt.toArray(amtArr);

                            final StoreAdapter OnlineOrders = new StoreAdapter(ShopOrder.this,idArr,billArr,itemArr,amtArr);
                            ShopOrder.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    orderList.setAdapter(OnlineOrders);
                                    orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            TextView tag = (TextView) view.findViewById(R.id.tag);
                                            prefs.edit().putString("store_tag", tag.getText().toString()).apply();
                                            Intent si = new Intent(ShopOrder.this,ShopInvoice.class);
                                            startActivity(si);
                                        }
                                    });
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
            ProgressDlg.dismissProgressDialog();
        }
    }

}
