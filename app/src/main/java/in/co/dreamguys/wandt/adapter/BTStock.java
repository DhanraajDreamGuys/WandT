package in.co.dreamguys.wandt.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import in.co.dreamguys.wandt.R;

public class BTStock extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] prod_id;
    private final String[] prod_name;
    private final String[] prod_qty;
    private final String[] prod_amt;
    private final String[] prod_img;
    DisplayImageOptions options;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    public BTStock(Activity context, String[] prod_id,String[] prod_name, String[] prod_qty,String[] prod_amt,String[] prod_img) {
        super(context, R.layout.stock_out_item, prod_id);
        this.context = context;
        this.prod_id = prod_id;
        this.prod_name = prod_name;
        this.prod_qty = prod_qty;
        this.prod_amt = prod_amt;
        this.prod_img = prod_img;
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(8)).build();
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.stock_out_item, null, true);

        TextView name_prod = (TextView) rowView.findViewById(R.id.stockoutID);
        TextView qty_prod = (TextView) rowView.findViewById(R.id.stockTotal);
        TextView date_prod = (TextView) rowView.findViewById(R.id.stockDate);
        TextView amt_prod = (TextView) rowView.findViewById(R.id.stockPrice);
        ImageView img_prod = (ImageView) rowView.findViewById(R.id.stock_icon);
        imageLoader.displayImage(prod_img[position], img_prod, options);
        //Bitmap bmp = BitmapFactory.decodeFile(prod_img[position]);
        //img_prod.setImageBitmap(bmp);
        name_prod.setText("Product ID: "+prod_id[position]);
        qty_prod.setText(prod_name[position]);
        date_prod.setText("Qty: "+prod_qty[position]);
        amt_prod.setText("Price: " + prod_amt[position]);
        return rowView;
    };
}
