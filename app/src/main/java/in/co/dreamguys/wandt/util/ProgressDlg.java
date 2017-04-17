package in.co.dreamguys.wandt.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by gopi.s on 09-Dec-15.
 */
public class ProgressDlg {

    private static ProgressDialog progressDialog = null;

    public static void showProgressDialog(Context context, String title, String message)
    {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog()
    {
        progressDialog.dismiss();
    }
    public static void clearDialog()
    {
        progressDialog = null;
    }

}