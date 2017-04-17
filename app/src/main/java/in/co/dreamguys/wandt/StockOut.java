package in.co.dreamguys.wandt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.co.dreamguys.wandt.util.DatabaseHelper;
import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.ProgressDlg;
import in.co.dreamguys.wandt.adapter.StockOutAdapter;
import in.co.dreamguys.wandt.adapter.StockSales;

public class StockOut extends Activity {

    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";

    private static final String TAG_TODAY = "today";
    private static final String TAG_SALES = "sales";
    private static final String TAG_SHOP = "shops";

    private static final String TAG_ID = "id";
    private static final String TAG_QTY = "qty";
    private static final String TAG_AMT = "amt";
    private static final String TAG_DATE = "date";

    private static final String HY_ID = "order_id";
    private static final String HY_NAME = "shop_name";
    private static final String HY_QTY = "so_qty";
    private static final String HY_AMT = "so_amt";
    private static final String HY_DATE = "so_date";


    String driver_id, app_url,url = "api/get_stock_details";
    String[] separated;
    TextView title;
    ListView tdayStock,salesStock;
    JSONArray today = null,sales = null,shop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.stock_out);

        //Tab host setup
        TabHost th = (TabHost) findViewById(android.R.id.tabhost);
        th.setup();

        //Stocks Tab
        TabHost.TabSpec tab1 = th.newTabSpec("tag1");
        tab1.setContent(R.id.tab1);
        tab1.setIndicator("Stocks");
        th.addTab(tab1);

        //History Tab
        TabHost.TabSpec tab2 = th.newTabSpec("tag2");
        tab2.setContent(R.id.tab2);
        tab2.setIndicator("History");
        th.addTab(tab2);

        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("STOCK OUT");

        //delete shop details
        DatabaseHelper emptyShop = new DatabaseHelper(StockOut.this);
        emptyShop.open();
        emptyShop.deleteShopDetails();
        emptyShop.close();

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        driver_id = prefs.getString("driver_id", "");
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;

        new StockTaskRunner().execute(app_url + "?driver_id=" + driver_id);
    }

    class StockTaskRunner extends AsyncTask<String,String,String>{
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(StockOut.this, "Loading", "Please wait...");
        }
        @Override
        protected String doInBackground(String... stock_url) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(stock_url[0]);
                try {
                    shop = json.getJSONArray(TAG_SHOP);
                    if(shop!=null) {
                        DatabaseHelper insertShop = new DatabaseHelper(StockOut.this);
                        insertShop.open();
                        for (int i = 0; i < shop.length(); i++) {
                            if (!shop.getString(i).isEmpty()) {
                                insertShop.insertShopDetails(shop.getString(i));
                            }
                        }
                        insertShop.close();
                    }

                    if(json!=null) {
                        // Getting JSON Array
                        try {
                            if (json.getString(TAG_SUCCESS).contains("y")) {
                                //history list
                                List<String> hy_id = new ArrayList<String>();
                                List<String> hy_name = new ArrayList<String>();
                                List<String> hy_qty = new ArrayList<String>();
                                List<String> hy_amt = new ArrayList<String>();
                                List<String> hy_date = new ArrayList<String>();
                                sales = json.getJSONArray(TAG_SALES);
                                for (int i = 0; i < sales.length(); i++) {
                                    JSONObject hsyChildNode = null;
                                    try {
                                        hsyChildNode = sales.getJSONObject(i);
                                        hy_id.add(hsyChildNode.getString(HY_ID));
                                        hy_name.add(hsyChildNode.getString(HY_NAME));
                                        hy_qty.add(hsyChildNode.getString(HY_QTY));
                                        hy_amt.add(hsyChildNode.getString(HY_AMT));
                                        hy_date.add(hsyChildNode.getString(HY_DATE));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                //list to array conversion

                                String[] hy_nameArr = new String[hy_name.size()];
                                hy_nameArr = hy_name.toArray(hy_nameArr);

                                String[] hy_idArr = new String[hy_id.size()];
                                hy_idArr = hy_id.toArray(hy_idArr);

                                String[] hy_qtyArr = new String[hy_qty.size()];
                                hy_qtyArr = hy_qty.toArray(hy_qtyArr);

                                String[] hy_amtArr = new String[hy_amt.size()];
                                hy_amtArr = hy_amt.toArray(hy_amtArr);

                                String[] hy_dateArr = new String[hy_date.size()];
                                hy_dateArr = hy_date.toArray(hy_dateArr);

                                final StockSales salesAdapter = new StockSales(StockOut.this, hy_idArr,hy_nameArr, hy_qtyArr, hy_amtArr, hy_dateArr);
                                StockOut.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        salesStock = (ListView) findViewById(R.id.historyStock);
                                        salesStock.setAdapter(salesAdapter);
                                        salesStock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                TextView tv = (TextView) view.findViewById(R.id.stockoutID);
                                                String soID = tv.getText().toString();
                                                separated = soID.split(":");
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("sales_order_id", separated[1]);
                                                editor.apply();
                                                Intent DetailsSale = new Intent(StockOut.this, SalesDetails.class);
                                                startActivity(DetailsSale);
                                            }
                                        });

                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e1) {
                    e1.printStackTrace();
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
            if(json!=null){
                // Getting JSON Array
                try {
                    if(json.getString(TAG_SUCCESS).contains("y")) {
                        //today list
                        List<String> t_id = new ArrayList<String>();
                        List<String> t_qty = new ArrayList<String>();
                        List<String> t_amt = new ArrayList<String>();
                        List<String> t_date = new ArrayList<String>();

                        today = json.getJSONArray(TAG_TODAY);
                        for (int i = 0; i < today.length(); i++) {
                            JSONObject todayChildNode = null;
                            try {
                                todayChildNode = today.getJSONObject(i);
                                // Storing  JSON item in a Variable
                                t_id.add(todayChildNode.getString(TAG_ID));
                                t_qty.add(todayChildNode.getString(TAG_QTY));
                                t_amt.add(todayChildNode.getString(TAG_AMT));
                                t_date.add(todayChildNode.getString(TAG_DATE));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //list to array conversion
                        String[] t_idArr = new String[t_id.size()];
                        t_idArr = t_id.toArray(t_idArr);
                        String[] t_qtyArr = new String[t_qty.size()];
                        t_qtyArr = t_qty.toArray(t_qtyArr);
                        String[] t_amtArr = new String[t_amt.size()];
                        t_amtArr = t_amt.toArray(t_amtArr);
                        String[] t_dateArr = new String[t_date.size()];
                        t_dateArr = t_date.toArray(t_dateArr);

                        final StockOutAdapter tdayAdapter = new StockOutAdapter(StockOut.this, t_idArr, t_qtyArr, t_amtArr, t_dateArr);
                        StockOut.this.runOnUiThread(new Runnable() {
                            public void run() {
                                tdayStock = (ListView) findViewById(R.id.tdayStock);
                                tdayStock.setAdapter(tdayAdapter);
                                tdayStock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        TextView tv = (TextView) view.findViewById(R.id.stockoutID);
                                        String soID = tv.getText().toString();
                                        separated = soID.split(":");
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("stock_id", separated[1]);
                                        editor.apply();
                                        Intent stockDetails = new Intent(StockOut.this, StockOutDetails.class);
                                        startActivity(stockDetails);
                                    }
                                });
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
