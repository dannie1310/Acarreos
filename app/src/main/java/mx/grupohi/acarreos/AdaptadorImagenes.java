package mx.grupohi.acarreos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by Usuario on 22/11/2016.
 */

public class AdaptadorImagenes extends BaseAdapter {

    private Context context;

    public AdaptadorImagenes(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        System.out.println("items "+ ImagenesViaje.ITEMS);
        return ImagenesViaje.ITEMS.length;
    }

    @Override
    public ImagenesViaje getItem(int position) {
        return ImagenesViaje.ITEMS[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.grid_item, viewGroup, false);
        }

        ImageView imagen = (ImageView) view.findViewById(R.id.imagen);
        TextView nombre = (TextView) view.findViewById(R.id.nombre);

        final ImagenesViaje item = getItem(position);


        System.out.println("selec "+item.getIdDrawable());
        String image = item.getIdDrawable();
       // Bitmap imagenUsar= Usuario.decodeBase64(image);

        /*final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[16 * 1024];

        Bitmap bitmap = BitmapFactory.decodeFile(image, options);*/
        imagen.setImageBitmap(
                decodeSampledBitmapFromResource(image, 100, 100));
        //imagen.setImageBitmap(bitmap);
       /* Glide.with(imagenCoche.getContext())
                .load(item.getIdDrawable())
                .into(imagenCoche);
*/
        nombre.setText(item.getNombre());

        return view;
    }

    public static Bitmap decodeSampledBitmapFromResource(String res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
