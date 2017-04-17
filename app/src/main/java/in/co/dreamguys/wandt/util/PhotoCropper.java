package in.co.dreamguys.wandt.util;

/*The MIT License (MIT)

Copyright (c) 2015 Issei Aoki

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import in.co.dreamguys.wandt.R;


public class PhotoCropper extends Activity {
    public Bitmap cropped = null;
    private CropImageView mCropView;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    File pathDirectory;
    public static final String PREFS_NAME = "WandTPrefs";
    SharedPreferences prefs;
    TextView title;
    Bitmap selectedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.photo_cropper);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        pathDirectory = new File(prefs.getString("pathDirectory", ""));
        File selectedFile = new  File(prefs.getString("BitmapPath", ""));
        findViews();
        if(selectedFile.exists()) {
            selectedBitmap = BitmapFactory.decodeFile(selectedFile.getAbsolutePath());
            mCropView.setImageBitmap(selectedBitmap);
        }
        mCropView.setMinFrameSizeInDp(200);
        mCropView.setInitialFrameScale(1.0f);
    }

    // Handle button event
    private final View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.buttonDone:
                   cropped = mCropView.getCroppedBitmap();
                    String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
                    File pictureFile;
                    String mImageName="BS_"+ timeStamp +".jpg";
                    pictureFile = new File(pathDirectory + File.separator + mImageName);
                    if (pictureFile == null) {
                        Toast.makeText(getApplicationContext(), "Error! Creating media file, check storage permissions", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        cropped.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.close();
                        prefs.edit().putString("pictureFile", String.valueOf(pictureFile)).apply();
                        Toast.makeText(getApplicationContext(), "Success! Image have been cropped", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Error! File not found", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Error accessing file", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.buttonChangeImage:
                    selectImage();
                    break;
                case R.id.buttonRotateImage:
                    mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                    break;
            }
        }
    };

    private void selectImage() {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(PhotoCropper.this);
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
        if (!((Activity) PhotoCropper.this).isFinishing()) {
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
        mCropView.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bmp = null;
        try {
            bmp = getBitmapFromUri(selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCropView.setImageBitmap(bmp);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void findViews() {
        mCropView = (CropImageView) findViewById(R.id.cropImageView);
        findViewById(R.id.buttonDone).setOnClickListener(btnListener);
        findViewById(R.id.buttonChangeImage).setOnClickListener(btnListener);
        findViewById(R.id.buttonRotateImage).setOnClickListener(btnListener);
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText("PRODUCT IMAGE");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        prefs.edit().putString("pictureFile", "").apply();
        finish();
    }
}