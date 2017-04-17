package in.co.dreamguys.wandt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.co.dreamguys.wandt.adapter.HomeAdapter;
import in.co.dreamguys.wandt.adapter.ShopAdapter;
import in.co.dreamguys.wandt.network.DownloadHelper;
import in.co.dreamguys.wandt.network.JSONParser;
import in.co.dreamguys.wandt.util.DatabaseHelper;

import static in.co.dreamguys.wandt.R.id.pos;

public class MainStockItem extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SHOP = "shops";

    private static final String TAG_STOCK = "products";
    private static final String TAG_STATUS = "status";
    private static final String TAG_ID = "prod_id";
    private static final String TAG_NAME = "prod_name";
    private static final String TAG_IMG = "prod_img";
    private static final String TAG_CODE = "prod_code";
    private static final String TAG_QTY = "prod_qty";
    private static final String TAG_AMT = "prod_amt";
    private static final String TAG_CAT = "prod_cat";

    TextView title;
    ListView allStock, historyStock;

    TabHost th;
    JSONArray stock = null, sales = null, shop = null;
    String driver_id, stock_id, app_url, url = "api/get_stock_and_history";
    File pathDirectory, root;
    SharedPreferences.Editor editor;
    Switch scan_switch;
    Button delivery_btn, return_btn, upload_btn, store_btn, mPos;
    ImageView imgV;
    private SwipeRefreshLayout swipeRefreshLayout;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_stock_items);
        //delete shop details
        DatabaseHelper emptyShop = new DatabaseHelper(MainStockItem.this);
        emptyShop.open();
        emptyShop.deleteShopDetails();
        emptyShop.deleteStockDetails();
        emptyShop.close();
        initTabs();
        initVars();
    }

    public void initTabs() {
        //Tab host setup
        th = (TabHost) findViewById(android.R.id.tabhost);
        th.setup();
        //Stocks Tab
        TabHost.TabSpec tab1 = th.newTabSpec("tag1");
        tab1.setContent(R.id.tab1);
        tab1.setIndicator(updateIndicator(th, R.drawable.s_marker));
        th.addTab(tab1);
        //scan Tab
        TabHost.TabSpec tab2 = th.newTabSpec("tag2");
        tab2.setContent(R.id.tab2);
        tab2.setIndicator(updateIndicator(th, R.drawable.u));
        th.addTab(tab2);
        //History Tab
        TabHost.TabSpec tab3 = th.newTabSpec("tag3");
        tab3.setContent(R.id.tab3);
        tab3.setIndicator(updateIndicator(th, R.drawable.u));
        th.addTab(tab3);

        th.getTabWidget().setDividerDrawable(null);
        th.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < th.getTabWidget().getChildCount(); i++) {
                    th.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_bg_unselected);
                }
                th.getTabWidget().getChildAt(th.getCurrentTab()).setBackgroundResource(R.drawable.tab_bg_selected);
            }
        });
    }


    public void initVars() {
        allStock = (ListView) findViewById(R.id.allStock);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("Home");
        mPos = (Button) findViewById(pos);
        mPos.setOnClickListener(this);
        delivery_btn = (Button) findViewById(R.id.delivery_btn);
        delivery_btn.setOnClickListener(this);
        return_btn = (Button) findViewById(R.id.return_btn);
        return_btn.setOnClickListener(this);
        upload_btn = (Button) findViewById(R.id.upload_btn);
        upload_btn.setOnClickListener(this);
        store_btn = (Button) findViewById(R.id.store_btn);
        store_btn.setOnClickListener(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        driver_id = prefs.getString("driver_id", "");
        stock_id = prefs.getString("stock_id", "");
        String tgl = prefs.getString("scan_switch", "");
        imgV = (ImageView) findViewById(R.id.imgV);
        scan_switch = (Switch) findViewById(R.id.scan_switch);
        scan_switch.setOnCheckedChangeListener(this);
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        new HomeTaskRunner().execute(app_url + "?driver_id=" + driver_id + "&stock_id=" + stock_id);
                                    }
                                }
        );

        if (Environment.getExternalStorageState() == null) {
            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            pathDirectory = contextWrapper.getDir("WandT", Context.MODE_PRIVATE);
        } else if (Environment.getExternalStorageState() != null) {
            root = Environment.getExternalStorageDirectory();
            if (root.canWrite()) {
                pathDirectory = new File(root.getAbsolutePath() + "/WandT");
                pathDirectory.mkdirs();
            }
        }
        editor = prefs.edit();
        editor.putString("pathDirectory", String.valueOf(pathDirectory));
        if (tgl.contentEquals("BT")) {
            editor.putString("scan_switch", "BT");
            scan_switch.setChecked(true);
            scan_switch.setText("BT");
            imgV.setImageResource(R.drawable.bar_code);
        } else {
            editor.putString("scan_switch", "CAMERA");
            scan_switch.setChecked(false);
            scan_switch.setText("CAMERA");
            imgV.setImageResource(R.drawable.qr_code);
        }
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        String scan_toggle = prefs.getString("scan_switch", "");
        switch (v.getId()) {
            case R.id.delivery_btn:
                Intent delivery_scan;
                if (scan_toggle.contentEquals("BT")) {
                    editor.putString("out_page", "deliveryBtn");
                    editor.apply();
                    delivery_scan = new Intent(MainStockItem.this, BTScan.class);
                } else {
                    editor.putString("out_page", "");
                    editor.apply();
                    delivery_scan = new Intent(MainStockItem.this, StockDeliveryScan.class);
                }
                editor.putString("out_page", "");
                editor.apply();
                startActivity(delivery_scan);
                break;
            case R.id.return_btn:
                Intent return_scan;
                if (scan_toggle.contentEquals("BT")) {
                    editor.putString("out_page", "returnBtn");
                    editor.apply();
                    return_scan = new Intent(MainStockItem.this, BTScan.class);
                } else {
                    editor.putString("out_page", "");
                    editor.apply();
                    return_scan = new Intent(MainStockItem.this, StockReturnScan.class);
                }
                editor.putString("out_page", "");
                editor.apply();
                startActivity(return_scan);
                break;
            case R.id.upload_btn:
                Intent upload_scan = new Intent(MainStockItem.this, UploadStock.class);
                startActivity(upload_scan);
                break;
            case R.id.store_btn:
                Intent store_order = new Intent(MainStockItem.this, ShopOrder.class);
                startActivity(store_order);
                break;

        }
    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            editor.putString("scan_switch", "BT");
            editor.apply();
            scan_switch.setText("BT");
            imgV.setImageResource(R.drawable.bar_code);
        } else {
            editor.putString("scan_switch", "CAMERA");
            editor.apply();
            scan_switch.setText("CAMERA");
            imgV.setImageResource(R.drawable.qr_code);
        }
    }

    public View updateIndicator(TabHost th, int iconTab) {
        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, th.getTabWidget(), false);
        ImageView Icon = (ImageView) tabIndicator.findViewById(R.id.tabIcon);
        Icon.setImageResource(iconTab);
        return tabIndicator;
    }

    /*
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        //delete shop & product details
        DatabaseHelper eraseShop = new DatabaseHelper(MainStockItem.this);
        eraseShop.open();
        eraseShop.deleteShopDetails();
        eraseShop.deleteStockDetails();
        eraseShop.close();
        new HomeTaskRunner().execute(app_url + "?driver_id=" + driver_id + "&stock_id=" + stock_id);
    }

    class HomeTaskRunner extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // showing refresh animation before making http call
            swipeRefreshLayout.setRefreshing(true);
            /*ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(MainStockItem.this, "Loading", "Please wait...");*/
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONParser jHelper = new JSONParser();
                json = jHelper.getJSONFromUrl(params[0]);
                //stock details insert and list view
                if (json.getString(TAG_SUCCESS).contains("y")) {
                    stock = json.getJSONArray(TAG_STOCK);
                    DatabaseHelper insertStock = new DatabaseHelper(MainStockItem.this);
                    insertStock.open();
                    List<String> p_id = new ArrayList<String>();
                    List<String> p_name = new ArrayList<String>();
                    List<String> p_img = new ArrayList<String>();
                    List<String> p_qty = new ArrayList<String>();
                    List<String> p_amt = new ArrayList<String>();
                    List<String> p_cat = new ArrayList<String>();
                    if (stock != null) {
                        for (int i = 0; i < stock.length(); i++) {
                            JSONObject stockChildNode = null;
                            try {
                                DownloadHelper dHelper = new DownloadHelper();
                                stockChildNode = stock.getJSONObject(i);
                                String img_url = stockChildNode.getString(TAG_IMG);
                                String fileName = img_url.substring(img_url.lastIndexOf('/') + 1, img_url.length());
                                File myFile = new File(pathDirectory + "/" + fileName);
                                String sd_prod_img = null;
                                if (!myFile.exists()) {
                                    //sd_prod_img = dHelper.getImageFromUrl(pathDirectory, fileName, img_url);
                                } else {
                                    //sd_prod_img = pathDirectory+"/"+fileName;
                                }
                                p_id.add(stockChildNode.getString(TAG_ID));
                                p_name.add(stockChildNode.getString(TAG_NAME));
                                p_img.add(img_url);
                                p_qty.add(stockChildNode.getString(TAG_QTY));
                                p_amt.add(stockChildNode.getString(TAG_AMT));
                                p_cat.add(stockChildNode.getString(TAG_CAT));

                                //insert stock details for scan
                                String sd_prod_id = stockChildNode.getString(TAG_ID);
                                String sd_prod_name = stockChildNode.getString(TAG_NAME);
                                String sd_prod_code = stockChildNode.getString(TAG_CODE);
                                String sd_prod_qty = stockChildNode.getString(TAG_QTY);
                                String sd_prod_amt = stockChildNode.getString(TAG_AMT);
                                insertStock.insertStockDetails(stock_id, sd_prod_id, sd_prod_name, sd_prod_code, sd_prod_qty, sd_prod_amt, img_url);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    insertStock.close();
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

                    final HomeAdapter allProducts = new HomeAdapter(MainStockItem.this, idArr, nameArr, qtyArr, amtArr, imgArr, catArr);
                    final String stStatus = json.getString(TAG_STATUS);
                    MainStockItem.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (stStatus.contains("1")) {
                                //fab.setVisibility(View.INVISIBLE);
                            }

                            allStock.setAdapter(allProducts);
                        }
                    });

                    //insert shop details
                    List<String> shop_id = new ArrayList<String>();
                    List<String> shop_name = new ArrayList<String>();

                    shop = json.getJSONArray(TAG_SHOP);
                    if (shop != null) {
                        DatabaseHelper insertShop = new DatabaseHelper(MainStockItem.this);
                        insertShop.open();
                        for (int i = 0; i < shop.length(); i++) {
                            if (!shop.getString(i).isEmpty()) {
                                String ShopString = shop.getString(i);
                                String[] separated = ShopString.split(":");
                                shop_id.add(separated[0]);
                                shop_name.add(separated[1]);
                                insertShop.insertShopDetails(separated[1]);
                            }
                        }
                        insertShop.close();
                    }
                    String[] shop_ideArr = new String[shop_id.size()];
                    shop_ideArr = shop_id.toArray(shop_ideArr);
                    String[] shop_nameArr = new String[shop_name.size()];
                    shop_nameArr = shop_name.toArray(shop_nameArr);

                    final ShopAdapter sh_adapter = new ShopAdapter(MainStockItem.this, shop_ideArr, shop_nameArr);
                    MainStockItem.this.runOnUiThread(new Runnable() {
                        public void run() {
                            historyStock = (ListView) findViewById(R.id.historyStock);
                            historyStock.setAdapter(sh_adapter);
                            historyStock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    TextView tv = (TextView) view.findViewById(R.id.stockoutID);
                                    String soID = tv.getText().toString();
                                    soID = soID.replace("#", "").trim();
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("shop_id_sales", soID);
                                    editor.apply();
                                    Intent DetailsOrder = new Intent(MainStockItem.this, OrderDetails.class);
                                    startActivity(DetailsOrder);
                                }
                            });
                        }
                    });
                }
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
            //ProgressDlg.dismissProgressDialog();
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
