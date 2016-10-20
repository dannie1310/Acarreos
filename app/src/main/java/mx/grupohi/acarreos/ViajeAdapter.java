package mx.grupohi.acarreos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Usuario on 19/10/2016.
 */

public class ViajeAdapter extends ArrayAdapter <Viaje> {

    public ViajeAdapter(Context context, List<Viaje> objects) {
        super(context, 0, objects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtener inflater.
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // ¿Existe el view actual?
        if (null == convertView) {
            convertView = inflater.inflate(
                    R.layout.list_item_viaje,
                    parent,
                    false);
        }


        TextView camion = (TextView) convertView.findViewById(R.id.idCamion);
        TextView material = (TextView) convertView.findViewById(R.id.idMaterial);
        TextView origen = (TextView) convertView.findViewById(R.id.idOrigen);
        TextView destino = (TextView) convertView.findViewById(R.id.idDestino);


        Viaje viaje = getItem(position);

        // Setup.

        camion.setText(viaje.camion.economico);
        material.setText(viaje.material.descripcion);
        origen.setText(viaje.origen.descripcion);
        destino.setText(viaje.tiro.descripcion);

        return convertView;
    }

}

