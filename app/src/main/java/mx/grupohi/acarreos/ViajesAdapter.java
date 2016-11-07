package mx.grupohi.acarreos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Creado por JFEsquivel on 20/10/2016.
 */

class ViajesAdapter extends ArrayAdapter<Viaje> {

    private Context context;

    ViajesAdapter(Context context, List<Viaje> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (null == convertView) {
            convertView = inflater.inflate(
                    R.layout.list_item_viaje,
                    parent,
                    false);
        }

        TextView camion = (TextView) convertView.findViewById(R.id.camion);
        TextView material = (TextView) convertView.findViewById(R.id.material);
        TextView origen = (TextView) convertView.findViewById(R.id.origen);
        TextView destino = (TextView) convertView.findViewById(R.id.destino);

        Viaje viaje = getItem(position);

        assert viaje != null;
        camion.setText(viaje.camion.economico);
        material.setText(viaje.material.descripcion);
        origen.setText(viaje.origen.descripcion);
        destino.setText(viaje.tiro.descripcion);

        return convertView;
    }
}
