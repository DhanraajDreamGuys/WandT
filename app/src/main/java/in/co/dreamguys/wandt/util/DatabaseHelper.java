package in.co.dreamguys.wandt.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gopi.s on 12-Dec-15.
 */
public class DatabaseHelper {
    private static final String DATABASE_NAME = "dev_wandt";

    private static final String DATABASE_TABLE_STOCK = "stock_details";
    public static final String ID = "sd_id";
    public static final String PROD_STOCK_ID = "sd_stock_id";
    public static final String PROD_ID = "sd_prod_id";
    public static final String PROD_NAME = "sd_prod_name";
    public static final String PROD_IMG = "sd_prod_img";
    public static final String PROD_CODE = "sd_prod_code";
    public static final String PROD_QTY = "sd_prod_qty";
    public static final String PROD_AMT = "sd_prod_amt";

    private static final String DATABASE_TABLE_DELIVERY = "delivery_scan_details";
    public static final String D_ID = "d_id";
    public static final String D_STOCK_ID = "d_stock_id";
    public static final String D_PROD_ID = "d_prod_id";
    public static final String D_PROD_CODE = "d_prod_code";
    public static final String D_PROD_QTY = "d_prod_qty";
    public static final String D_PROD_PRICE = "d_prod_price";

    private static final String DATABASE_TABLE_RETURN = "return_scan_details";
    public static final String R_ID = "r_id";
    public static final String R_STOCK_ID = "r_stock_id";
    public static final String R_PROD_ID = "r_prod_id";
    public static final String R_PROD_CODE = "r_prod_code";
    public static final String R_PROD_QTY = "r_prod_qty";

    private static final String DATABASE_TABLE_SHOP = "shop_details";
    public static final String SH_ID = "sh_id";
    public static final String SH_NAME = "sh_name";

    private static final int DATABASE_VERSION = 1;

    private Database ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    public DatabaseHelper(Context c ){
        ourContext = c;
    }

    public DatabaseHelper open() throws SQLException {
        ourHelper = new Database(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
    }

    private static class Database extends SQLiteOpenHelper {
        public Database(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_STOCK + " (" + ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PROD_STOCK_ID + " BIGINT, " + PROD_NAME + " VARCHAR, " + PROD_IMG
                    + " LONGTEXT, " + PROD_ID + " BIGINT, " + PROD_CODE + " VARCHAR, "+ PROD_QTY +" BIGINT, "+ PROD_AMT +" DECIMAL);");

            db.execSQL("CREATE TABLE " + DATABASE_TABLE_DELIVERY + " (" + D_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + D_STOCK_ID
                    + " BIGINT, " + D_PROD_ID + " BIGINT, " + D_PROD_CODE + " VARCHAR, "+ D_PROD_QTY +" BIGINT, "+D_PROD_PRICE+" DECIMAL);");

            db.execSQL("CREATE TABLE " + DATABASE_TABLE_RETURN + " (" + R_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + R_STOCK_ID
                    + " BIGINT, " + R_PROD_ID + " BIGINT, " + R_PROD_CODE + " VARCHAR, "+ R_PROD_QTY +" BIGINT);");

            db.execSQL("CREATE TABLE " + DATABASE_TABLE_SHOP + " (" + SH_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SH_NAME + " VARCHAR);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXIST " + DATABASE_TABLE_STOCK);
            db.execSQL("DROP TABLE IF EXIST " + DATABASE_TABLE_DELIVERY);
            db.execSQL("DROP TABLE IF EXIST " + DATABASE_TABLE_RETURN);
            db.execSQL("DROP TABLE IF EXIST " + DATABASE_TABLE_SHOP);
            onCreate(db);
        }
    }

    //shop details
    public long insertShopDetails(String sh_name){
        ContentValues cv = new ContentValues();
        cv.put(SH_NAME, sh_name);
        return ourDatabase.insert(DATABASE_TABLE_SHOP, null, cv);
    }

    public void deleteShopDetails()throws SQLException{
        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.delete(DATABASE_TABLE_SHOP, null, null);
        ourDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SHOP);
        ourDatabase.execSQL("CREATE TABLE " + DATABASE_TABLE_SHOP + " (" + SH_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SH_NAME + " VARCHAR);");
        ourDatabase.close();
    }

    public JSONArray getShopDetails() throws JSONException {
        String Query = "SELECT "+SH_NAME+" FROM " + DATABASE_TABLE_SHOP;
        Cursor cursor = ourDatabase.rawQuery(Query, null);
        int s1 = cursor.getColumnIndex(SH_NAME);
        JSONObject jResult = new JSONObject();
        JSONArray jArray = new JSONArray();
        if(cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                jArray.put(cursor.getString(s1));
            }
        }
        //jResult.put("shop_sync",jArray);
        return jArray;
    }

    //stock details
    public long insertStockDetails(String sd_stock_id, String sd_prod_id,String sd_prod_name,String sd_prod_code, String sd_prod_qty,String sd_prod_amt,String sd_prod_img){
        ContentValues cv = new ContentValues();
        cv.put(PROD_STOCK_ID, sd_stock_id);
        cv.put(PROD_ID,sd_prod_id);
        cv.put(PROD_NAME, sd_prod_name);
        cv.put(PROD_IMG, sd_prod_img);
        cv.put(PROD_CODE, sd_prod_code);
        cv.put(PROD_QTY, sd_prod_qty);
        cv.put(PROD_AMT, sd_prod_amt);
        return ourDatabase.insert(DATABASE_TABLE_STOCK, null, cv);
    }

    public String checkStockQty(String editProd){
        String result ="";
        String Query = "SELECT "+PROD_QTY+" FROM " + DATABASE_TABLE_STOCK +" WHERE "+PROD_ID +"='"+editProd+"'";
        Cursor cursor = ourDatabase.rawQuery(Query, null);
        while (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }

    public void deleteStockDetails()throws SQLException{
        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.delete(DATABASE_TABLE_STOCK, null, null);
        ourDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_STOCK);
        ourDatabase.execSQL("CREATE TABLE " + DATABASE_TABLE_STOCK + " (" + ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PROD_STOCK_ID + " BIGINT, " + PROD_NAME + " VARCHAR, " + PROD_IMG
                + " LONGTEXT, " + PROD_ID + " BIGINT, " + PROD_CODE + " VARCHAR, " + PROD_QTY + " BIGINT, " + PROD_AMT + " DECIMAL);");
        ourDatabase.close();
    }

    public void checkStockDetails(){
        String Query = "SELECT * FROM stock_details";
        Cursor cursor = ourDatabase.rawQuery(Query, null);
        int i1 = cursor.getColumnIndex(PROD_STOCK_ID);
        int i2 = cursor.getColumnIndex(PROD_ID);
        int i3 = cursor.getColumnIndex(PROD_NAME);
        int i4 = cursor.getColumnIndex(PROD_CODE);
        int i5 = cursor.getColumnIndex(PROD_QTY);
        int i6 = cursor.getColumnIndex(PROD_AMT);
        if(cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                System.out.println("qty: "+cursor.getString(i5));
                System.out.println("code: "+cursor.getString(i4));
            }
        }
        cursor.close();
    }

    public ArrayList<String> getStockDetails(String code){
        String Query = "SELECT * FROM " + DATABASE_TABLE_STOCK +" WHERE sd_prod_code='"+code+"' AND sd_prod_qty != '0' ";
        Cursor cursor = ourDatabase.rawQuery(Query, null);
        //ArrayList<ArrayList<String>> main_arr=new ArrayList<ArrayList<String>>();
        ArrayList<String> sub_arr = new ArrayList<String>();
        int i1 = cursor.getColumnIndex(PROD_STOCK_ID);
        int i2 = cursor.getColumnIndex(PROD_ID);
        int i3 = cursor.getColumnIndex(PROD_NAME);
        int i4 = cursor.getColumnIndex(PROD_CODE);
        int i5 = cursor.getColumnIndex(PROD_QTY);
        int i6 = cursor.getColumnIndex(PROD_AMT);
        int i7 = cursor.getColumnIndex(PROD_IMG);
        if(cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                sub_arr.add(cursor.getString(i2));
                sub_arr.add(cursor.getString(i3));
                sub_arr.add(cursor.getString(i4));
                sub_arr.add(cursor.getString(i6));
                sub_arr.add(cursor.getString(i5));
                sub_arr.add(cursor.getString(i7));
            }
        }
        cursor.close();
        return sub_arr;
    }

    public String getStockName(String _id){
        String Query = "SELECT * FROM " + DATABASE_TABLE_STOCK +" WHERE sd_prod_id='"+_id+"'";
        Cursor cursor = ourDatabase.rawQuery(Query, null);
        String name_str = "";
        int i1 = cursor.getColumnIndex(PROD_NAME);
        if(cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                name_str = cursor.getString(i1);
            }
        }
        cursor.close();
        return name_str;
    }

    //delivery details
    public long insertDeliveryDetails(String d_stock_id, String d_prod_id,String d_prod_code, String d_prod_qty, String d_prod_price){
        ContentValues cv = new ContentValues();
        cv.put(D_STOCK_ID,d_stock_id);
        cv.put(D_PROD_ID,d_prod_id);
        cv.put(D_PROD_CODE,d_prod_code);
        cv.put(D_PROD_QTY, d_prod_qty);
        cv.put(D_PROD_PRICE, d_prod_price);
        return ourDatabase.insert(DATABASE_TABLE_DELIVERY, null, cv);
    }

    public JSONObject getDeliveryDetails(String stock_id,String delivery_to, String discount, String driver_id) throws JSONException {
        String Query = "SELECT * FROM " + DATABASE_TABLE_DELIVERY;
        Cursor cursor = ourDatabase.rawQuery(Query, null);

        int i1 = cursor.getColumnIndex(D_PROD_ID);
        int i2 = cursor.getColumnIndex(D_PROD_QTY);
        double i3 = cursor.getColumnIndex(D_PROD_PRICE);
        JSONObject jResult = new JSONObject();
        jResult.putOpt("stock_id",stock_id);
        jResult.putOpt("driver_id",driver_id);
        jResult.putOpt("delivery_to",delivery_to);
        jResult.putOpt("discount",discount);
        JSONArray jArray = new JSONArray();
        int tot_qty = 0;
        if(cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                JSONObject jGroup = new JSONObject();
                jGroup.put("prod_id", cursor.getString(i1));
                jGroup.put("prod_qty", cursor.getString(i2));
                jGroup.put("prod_rs", cursor.getString((int) i3));
                jArray.put(jGroup);
                tot_qty = Integer.parseInt(String.valueOf(tot_qty)) + Integer.parseInt(cursor.getString(i2));
            }
        }
        jResult.put("delivery_qty", tot_qty);
        jResult.put("delivery_sync", jArray);
        return jResult;
    }

    public void deleteDeliveryDetails()throws SQLException{
        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.delete(DATABASE_TABLE_DELIVERY, null, null);
        ourDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_DELIVERY);
        ourDatabase.execSQL("CREATE TABLE " + DATABASE_TABLE_DELIVERY + " (" + D_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + D_STOCK_ID
                + " BIGINT, " + D_PROD_ID + " BIGINT, " + D_PROD_CODE + " VARCHAR, "+ D_PROD_QTY +" BIGINT, "+D_PROD_PRICE+" DECIMAL);");
        ourDatabase.close();
    }

    //return details
    public long insertReturnDetails(String r_stock_id, String r_prod_id,String r_prod_code, String r_prod_qty){
        ContentValues cv = new ContentValues();
        cv.put(R_STOCK_ID, r_stock_id);
        cv.put(R_PROD_ID, r_prod_id);
        cv.put(R_PROD_CODE, r_prod_code);
        cv.put(R_PROD_QTY, r_prod_qty);
        return ourDatabase.insert(DATABASE_TABLE_RETURN, null, cv);
    }

    public JSONObject getReturnDetails(String stock_id, String driver_id) throws JSONException {
        String Query = "SELECT * FROM " + DATABASE_TABLE_RETURN;
        Cursor cursor = ourDatabase.rawQuery(Query, null);

        int i1 = cursor.getColumnIndex(R_PROD_ID);
        int i2 = cursor.getColumnIndex(R_PROD_QTY);
        JSONObject jResult = new JSONObject();
        jResult.putOpt("stock_id",stock_id);
        jResult.putOpt("driver_id",driver_id);
        JSONArray jArray = new JSONArray();
        int tot_qty = 0;
        if(cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                JSONObject jGroup = new JSONObject();
                jGroup.put("prod_id", cursor.getString(i1));
                jGroup.put("prod_qty", cursor.getString(i2));
                tot_qty = Integer.parseInt(String.valueOf(tot_qty)) + Integer.parseInt(cursor.getString(i2));
                jArray.put(jGroup);
            }
        }
        jResult.put("return_qty", tot_qty);
        jResult.put("return_sync",jArray);
        return jResult;
    }

    public void deleteReturnDetails()throws SQLException{
        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.delete(DATABASE_TABLE_RETURN, null, null);
        ourDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_RETURN);
        ourDatabase.execSQL("CREATE TABLE " + DATABASE_TABLE_RETURN + " (" + R_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + R_STOCK_ID
                + " BIGINT, " + R_PROD_ID + " BIGINT, " + R_PROD_CODE + " VARCHAR, "+ R_PROD_QTY +" BIGINT);");
        ourDatabase.close();
    }

}
