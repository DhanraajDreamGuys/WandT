package in.co.dreamguys.wandt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.zj.btsdk.BluetoothService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import in.co.dreamguys.wandt.util.DatabaseHelper;
import in.co.dreamguys.wandt.util.DeviceListActivity;

public class OrderPrinter extends Activity implements View.OnClickListener {
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;

    Button closeBtn, connect_btn, print_btn;
    String stock_id, totQty, totAmt,driver_id, po_delivery, po_dis_per, pt_dis_amt, pt_total,driver_name;
    TextView title;
    ListView itemList;
    DecimalFormat dec = new DecimalFormat("#.00");
    ArrayList<String> i_id = new ArrayList<String>();
    ArrayList<String> i_name = new ArrayList<String>();
    ArrayList<String> i_qty = new ArrayList<String>();
    ArrayList<String> i_rs = new ArrayList<String>();

    //printer setup
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.order_printer);
        //retrieve delivery details
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stock_id = prefs.getString("stock_id", "").trim();
        driver_id = prefs.getString("driver_id", "").trim();
        po_delivery = prefs.getString("po_delivery", "");
        totQty = prefs.getString("totQty", "");
        totAmt = prefs.getString("totAmt", "");
        po_dis_per = prefs.getString("po_dis_per", "");
        pt_dis_amt = prefs.getString("pt_dis_amt", "");
        pt_total = prefs.getString("pt_total", "");
        driver_name = prefs.getString("driver_name", "");

        //toolbar title setup
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("SUCCESS");
        initializeVars();
        mService = new BluetoothService(this, mHandler);
        if( mService.isAvailable() == false ){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
    }

    public void initializeVars(){
        itemList = (ListView)findViewById(R.id.itemList);
        DatabaseHelper getItem = new DatabaseHelper(OrderPrinter.this);
        getItem.open();
        JSONObject json = null;
        JSONArray items = null;
        i_name.add("NAME");
        i_qty.add("QTY");
        i_rs.add("PRICE");
        try {
            json =  getItem.getDeliveryDetails(stock_id, po_delivery, po_dis_per, driver_id);
            items = json.getJSONArray("delivery_sync");
            if(items!=null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject itemChildNode = null;
                    itemChildNode = items.getJSONObject(i);
                    String _id = itemChildNode.getString("prod_id");
                    String _qty = itemChildNode.getString("prod_qty");
                    double _rs = Double.parseDouble(itemChildNode.getString("prod_rs"));
                    String _name = getItem.getStockName(_id);
                    i_name.add(_name);
                    i_qty.add(_qty);
                    i_rs.add(String.valueOf(_rs));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getItem.close();

        i_name.add("Items (QTY)");
        i_qty.add("");
        i_rs.add(totQty);

        i_name.add("Products");
        i_qty.add("");
        i_rs.add("\u00a3"+totAmt);

        i_name.add("Discount (%)");
        i_qty.add("");
        i_rs.add(po_dis_per);

        i_name.add("");
        i_qty.add("");
        i_rs.add(pt_dis_amt);

        i_name.add("Total");
        i_qty.add("");
        i_rs.add("\u00a3" + pt_total);

        String [] arr_name = i_name.toArray(new String[i_name.size()]);
        String [] arr_qty = i_qty.toArray(new String[i_qty.size()]);
        String [] arr_rs = i_rs.toArray(new String[i_rs.size()]);
        itemDetails printItems = new itemDetails(this,arr_name,arr_qty,arr_rs);
        itemList.setAdapter(printItems);
        connect_btn = (Button) findViewById(R.id.connect_btn);
        connect_btn.setOnClickListener(this);
        print_btn = (Button) findViewById(R.id.print_btn);
        print_btn.setOnClickListener(this);
        closeBtn = (Button) findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(this);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (mService.isBTopen() == false) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        try {
            if(mService.isBTopen() == false){
                print_btn.setEnabled(false);
            }else{
                print_btn.setEnabled(true);
            }
            if(con_dev == null){
                print_btn.setEnabled(false);
            }else{
                print_btn.setEnabled(true);
            }

        }catch (Exception ex) {
            Log.e("error",ex.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_btn:
                Intent serverIntent = new Intent(OrderPrinter.this,DeviceListActivity.class);
                startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
                break;
            case R.id.print_btn:
                String item_str1 = "";
                    byte[] cmd = new byte[3];
                    cmd[0] = 0x1b;
                    cmd[1] = 0x21;
                    cmd[2] |= 0x10;
                    mService.write(cmd);
                    mService.sendMessage("WANDT!\n", "GBK");
                    cmd[2] &= 0xEF;
                    mService.write(cmd);
                    item_str1 = "Receipient: "+po_delivery+"\n\n";
                    item_str1 += "Driver: "+driver_name+"\n\n";
                    DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                    String cur_date = df.format(Calendar.getInstance().getTime());
                    item_str1 += cur_date+"\n\n";
                    DatabaseHelper getItem = new DatabaseHelper(OrderPrinter.this);
                    getItem.open();
                    JSONObject json = null;
                    JSONArray items = null;
                    item_str1 += "-------------------------------\n";
                    item_str1 += "ITEM                 QTY  PRICE\n";
                    item_str1 += "-------------------------------\n";
                    //mService.sendMessage(item_str1, "GBK");
                    String item_str2 = "";
                    try {
                        json =  getItem.getDeliveryDetails(stock_id, po_delivery, po_dis_per, driver_id);
                        items = json.getJSONArray("delivery_sync");
                        if(items!=null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject itemChildNode = null;
                                itemChildNode = items.getJSONObject(i);
                                String _id = itemChildNode.getString("prod_id");
                                String _qty = itemChildNode.getString("prod_qty").trim();
                                String _rs = itemChildNode.getString("prod_rs").trim();
                                String _name = getItem.getStockName(_id);
                                StringBuilder rackingSystemSb = new StringBuilder(_name.toLowerCase());
                                rackingSystemSb.setCharAt(0, Character.toUpperCase(rackingSystemSb.charAt(0)));
                                _name = rackingSystemSb.toString();
                                if(_name.length() < 15){
                                    int as1 = (int) 15 - (int)_name.length();
                                    String txt1 = "";
                                    for(int j = 0; j < as1; j++){
                                        txt1 +=" ";
                                    }
                                    _name += txt1;
                                }
                                try{
                                    _name = rackingSystemSb.toString().substring(0,10)+"..";
                                }catch (StringIndexOutOfBoundsException e){
                                }

                                float _ra = Float.parseFloat(_rs);
                                String ind_rs = "("+dec.format(_ra)+")";
                                if(ind_rs.length() < 8) {
                                    int as3 = (int) 12 - (int)ind_rs.length();
                                    String txt3 = "";
                                    for(int l = 0; l < as3; l++){
                                        txt3 +=" ";
                                    }
                                    ind_rs +=txt3;
                                }else{
                                    ind_rs +="    ";
                                }
                                float fi_amt = Integer.parseInt(_qty) * _ra ;

                                if(_qty.length() < 3){
                                    int as2 = (int) 3 - (int)_qty.length();
                                    String txt2 = "";
                                    for(int k = 0; k < as2; k++){
                                        txt2 +=" ";
                                    }
                                    _qty += txt2;
                                }
                                item_str1 += _name+"\n";
                                item_str1 += ind_rs+"         "+_qty+"   "+dec.format(fi_amt)+"\n";
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //System.out.println(item_str1);
                    mService.sendMessage(item_str1, "GBK");
                    getItem.close();
                    String item_str3 = "";
                    if(!pt_dis_amt.isEmpty()){
                        pt_dis_amt = pt_dis_amt.replace("£","");
                        pt_dis_amt = pt_dis_amt.replace("-","");
                        float _da  = Float.parseFloat(pt_dis_amt);
                        pt_dis_amt = "-"+dec.format(_da);
                    }else{
                        po_dis_per = "";
                    }
                    pt_total = pt_total.replace("£", "");
                    float _pa  = Float.parseFloat(pt_total);
                    pt_total = dec.format(_pa);
                    item_str3 += "-------------------------------\n";
                    item_str3 += "Discount(%)          "+po_dis_per+"    "+pt_dis_amt+"\n";
                    //item_str3 += "Total Qty            "+totQty+"\n";
                    //item_str3 += "Products             "+totAmt+"\n";
                    item_str3 += "Total                "+totQty+"    "+pt_total+"\n";
                    item_str3 += "-------------------------------\n";
                    //System.out.println(item_str3);
                    mService.sendMessage(item_str3, "GBK");
                break;
            case R.id.closeBtn:
                Intent stOut = new Intent(OrderPrinter.this,MainStockItem.class);
                startActivity(stOut);
                break;
        }
    }

    /* Handler BluetoothService */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            print_btn.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("1", "connecting..");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.d("2","none..");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    print_btn.setEnabled(false);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                } else {
                    //finish();
                }
                break;
            case  REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    con_dev = mService.getDevByMac(address);
                    mService.connect(con_dev);
                }
                break;
        }
    }

    public  class itemDetails extends BaseAdapter {

        private Context yContext;
        private final String[] item_name;
        private final String[] item_qty;
        private final String[] item_rs;

        public itemDetails(Context context, String[] item_name,String[] item_qty, String[] item_rs) {
            yContext = context;
            this.item_name = item_name;
            this.item_qty = item_qty;
            this.item_rs = item_rs;
        }
        @Override
        public int getCount() {
            return item_name.length;
        }
        @Override
        public Object getItem(int position) {
            return item_name[position];
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View dl;
            LayoutInflater inflater = (LayoutInflater) yContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                dl = new View(yContext);
                dl = inflater.inflate(R.layout.print_item,parent, false);
            } else {
                dl = (View) convertView;
            }
            TextView tvName = (TextView)dl.findViewById(R.id._item);
            TextView tvQty = (TextView)dl.findViewById(R.id._qty);
            TextView tvRs = (TextView)dl.findViewById(R.id._price);
            String iN = item_name[position];
            if(iN.contentEquals("NAME")){
                tvName.setTypeface(null, Typeface.BOLD);
                tvName.setTextColor(Color.parseColor("#000000"));
                tvQty.setTypeface(null, Typeface.BOLD);
                tvQty.setTextColor(Color.parseColor("#000000"));
                tvRs.setTypeface(null, Typeface.BOLD);
                tvRs.setTextColor(Color.parseColor("#000000"));
            }
            if(iN.contentEquals("Items (QTY)") || iN.contentEquals("Products") || iN.contentEquals("") || iN.contentEquals("Total") || iN.contentEquals("Discount (%)")){
                tvName.setTypeface(null, Typeface.BOLD);
            }
            tvName.setText(item_name[position]);
            tvQty.setText(item_qty[position]);
            tvRs.setText(item_rs[position]);
            return dl;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent stOut = new Intent(OrderPrinter.this,MainStockItem.class);
        startActivity(stOut);
    }
}
