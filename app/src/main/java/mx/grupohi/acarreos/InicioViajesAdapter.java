package mx.grupohi.acarreos;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by DBENITEZ on 24/04/2017.
 */

public class InicioViajesAdapter extends ArrayAdapter<InicioViaje> {

    private Context context;

    InicioViajesAdapter(Context context, List<InicioViaje> objects) {
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
       // TextView destino = (TextView) convertView.findViewById(R.id.destino);

        InicioViaje viaje = getItem(position);

        assert viaje != null;
        if(viaje.camion != null) {
            camion.setText(viaje.camion.economico);
        }else{
            camion.setText("Num camión: "+viaje.idcamion);
        }
        if(viaje.material != null) {
            material.setText(viaje.material.descripcion);
        }else{
            material.setText("NO ENCONTRADO");
        }
        if(viaje.origen != null) {
            origen.setText(viaje.origen.descripcion);
        }else{
            origen.setText("NO ENCONTRADO");
        }
      //  destino.setText(viaje.tiro.descripcion);

        return convertView;
    }
}

