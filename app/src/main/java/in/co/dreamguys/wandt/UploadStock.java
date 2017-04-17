package in.co.dreamguys.wandt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import in.co.dreamguys.wandt.util.PhotoCropper;
import in.co.dreamguys.wandt.util.ProgressDlg;
import in.co.dreamguys.wandt.network.UploadHelper;

public class UploadStock extends Activity implements View.OnClickListener{
    private static final String TAG_SUCCESS = "success";

    Button uploadBtn,cancelBtn;
    TextView title;
    EditText itemName, itemQty, itemPrice, itemSale, itemCode;
    String Name, Qty, Price, Sale, Code, app_url, url ="api/upload_stock";
    ImageView toolIcon;
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    File root, pathDirectory;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.add_stock);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        pathDirectory = new File(prefs.getString("pathDirectory", ""));
        app_url = prefs.getString("api_url", "");
        app_url = app_url + url;
        initializeVars();
    }

    public void initializeVars(){
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("ADD STOCK");
        uploadBtn = (Button)findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(this);
        cancelBtn = (Button)findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);
        itemName = (EditText) findViewById(R.id.itemName);
        itemQty = (EditText) findViewById(R.id.itemQty);
        itemPrice = (EditText) findViewById(R.id.itemPrice);
        itemSale = (EditText) findViewById(R.id.itemSale);
        itemCode = (EditText) findViewById(R.id.itemCode);
        toolIcon = (ImageView) findViewById(R.id.toolIcon);
        toolIcon.setImageResource(R.drawable.clip);
        toolIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()) {
            case R.id.uploadBtn:

                root = new File(prefs.getString("pictureFile", ""));

                Name = itemName.getText().toString().trim();
                Qty = itemQty.getText().toString().trim();
                Price = itemPrice.getText().toString().trim();
                Sale = itemSale.getText().toString().trim();
                Code = itemCode.getText().toString().trim();
                Code = Code.replace(" ","");
                if(Name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter product name", Toast.LENGTH_LONG).show();
                }else if(Qty.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter QTY", Toast.LENGTH_LONG).show();
                }else if(Price.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter received price", Toast.LENGTH_LONG).show();
                }else if(Sale.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter selling price", Toast.LENGTH_LONG).show();
                }else if(Code.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter barcode", Toast.LENGTH_LONG).show();
                }else{
                    JSONObject jGroup = new JSONObject();
                    try {
                        jGroup.put("name", Name);
                        jGroup.put("qty", Qty);
                        jGroup.put("r_price", Price);
                        jGroup.put("s_price", Sale);
                        jGroup.put("code", Code);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new StockOutDeliveryTaskRunner().execute(app_url, jGroup.toString(),String.valueOf(root));
                }
                break;
            case R.id.cancelBtn:
                prefs.edit().putString("pictureFile", "").apply();
                finish();
                break;
           case R.id.toolIcon:
               setupDialogue();
               break;
        }
    }

    public void setupDialogue(){
        final String[] items = {"Camera","Gallery","Cancel"};
        final int[] icons = {
                R.drawable.dlgcamera,
                R.drawable.dlggallery,
                R.drawable.dlgcancel};

        ListAdapter dlgAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.dialogue_item, items) {
            ViewHolder holder;
            class ViewHolder {
                ImageView icon;
                TextView title;
            }
            public View getView(int position, View convertView, ViewGroup parent) {
                final LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.dialogue_item, null);
                    holder = new ViewHolder();
                    holder.icon = (ImageView) convertView.findViewById(R.id.dlgImg);
                    holder.title = (TextView) convertView.findViewById(R.id.dlgTv);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.title.setText(items[position]);
                holder.icon.setImageResource(icons[position]);
                return convertView;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(UploadStock.this);
        builder.setTitle("Product Photo");
        builder.setAdapter(dlgAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (which == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                }
            }
        });
        builder.create();
        if (!((Activity) UploadStock.this).isFinishing()) {
            builder.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (thumbnail != null) {
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        }
        File destination = new File(pathDirectory,System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prefs.edit().putString("BitmapPath", String.valueOf(destination)).apply();
        Intent pc1 = new Intent(UploadStock.this, PhotoCropper.class);
        startActivity(pc1);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        prefs.edit().putString("BitmapPath", String.valueOf(picturePath)).apply();
        Intent pc2 = new Intent(UploadStock.this, PhotoCropper.class);
        startActivity(pc2);
    }

    class StockOutDeliveryTaskRunner extends AsyncTask<String, String, String> {
        JSONObject json = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDlg.clearDialog();
            ProgressDlg.showProgressDialog(UploadStock.this, "Uploading", "Please wait...");
        }
        @Override
        protected String doInBackground(String... stock_url) {
            try {
                UploadHelper jHelper = new UploadHelper();
                json = jHelper.getJSONFromUrl(stock_url[0], stock_url[1], stock_url[2]);
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
                try {
                    if (json.getString(TAG_SUCCESS).contains("y")) {
                        itemName.setText("");
                        itemQty.setText("");
                        itemPrice.setText("");
                        itemSale.setText("");
                        itemCode.setText("");
                        Toast.makeText(getApplicationContext(), "Success. Product have been uploaded", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Error. Please try again", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        prefs.edit().putString("pictureFile", "").apply();
        finish();
    }

}
