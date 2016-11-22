package mx.grupohi.acarreos;

import android.content.Context;
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

        ImageView imagenCoche = (ImageView) view.findViewById(R.id.imagen);
        TextView nombreCoche = (TextView) view.findViewById(R.id.nombre);

        final ImagenesViaje item = getItem(position);
        Glide.with(imagenCoche.getContext())
                .load(item.getIdDrawable())
                .into(imagenCoche);

        nombreCoche.setText(item.getNombre());

        return view;
    }
}
