package in.co.dreamguys.wandt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import in.co.dreamguys.wandt.R;

public class HomeAdapter extends BaseAdapter {
    private Context yContext;
    private final String[] prod_id;
    private final String[] prod_name;
    private final String[] prod_price;
    private final String[] prod_qty;
    private final String[] prod_img;
    private final String[] prod_cat;

    DisplayImageOptions options;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    public HomeAdapter(Context context, String[] prod_id, String[] prod_name, String[] prod_qty, String[] prod_price,String[] prod_img,String[] prod_cat) {
        yContext = context;
        this.prod_id = prod_id;
        this.prod_name = prod_name;
        this.prod_price = prod_price;
        this.prod_qty = prod_qty;
        this.prod_img = prod_img;
        this.prod_cat = prod_cat;
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(8)).build();
    }

    @Override
    public int getCount()
    {
        return  prod_id.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View productList;
        LayoutInflater inflater = (LayoutInflater) yContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            productList = new View(yContext);
            productList = inflater.inflate(R.layout.home_view, null);
        } else {
            productList = (View) convertView;
        }
        TextView product_name = (TextView)productList.findViewById(R.id.product_name);
        TextView product_cat = (TextView)productList.findViewById(R.id.product_category);
        TextView product_qty = (TextView)productList.findViewById(R.id.product_qty);
        TextView product_amt = (TextView)productList.findViewById(R.id.product_amt);
        ImageView thumbnail = (ImageView) productList.findViewById(R.id.thumbnail);

        product_name.setText(prod_name[position]);
        product_cat.setText(prod_cat[position]);
        product_qty.setText(prod_qty[position]);
        product_amt.setText("\u00a3"+prod_price[position]);
        imageLoader.displayImage(prod_img[position], thumbnail, options);
        productList.setTag(prod_id[position]);
        return productList;
    }
}