package in.co.dreamguys.wandt.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class UploadHelper {
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    Bitmap mUserBitmap = null;
    Bitmap image;
    String responseValue;

    // constructor
    public UploadHelper() {

    }

    public JSONObject getJSONFromUrl(String url, String json_str, String prodImg) {

        // Making HTTP request
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost response = new HttpPost(url);
            MultipartEntity reqEntity = new MultipartEntity();
            if (!prodImg.isEmpty()) {
                image = getBitmap(prodImg);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                byte[] data = bos.toByteArray();
                ByteArrayBody bab = new ByteArrayBody(data, "prod_img.jpg");
                reqEntity.addPart("product_file", bab);
            }
            StringBody contentString = new StringBody(json_str);
            reqEntity.addPart("json_str", contentString);
            response.setEntity(reqEntity);
            BasicResponseHandler resHandler = new BasicResponseHandler();
            responseValue = httpClient.execute(response, resHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            jObj = new JSONObject(responseValue);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    private Bitmap getBitmap(String path) {
        //System.out.println("Get bitmap for path " + path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        mUserBitmap = BitmapFactory.decodeFile(path, bmOptions);
        mUserBitmap = Bitmap.createScaledBitmap(mUserBitmap, 500, 500, true);
        return mUserBitmap;
    }
}


