package cl.inexcell.exploradorperu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class DetalleElectrico extends Activity {

    String vendor;
    String dslam;
    String model;
    String datos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_detalle_electrico);
        Intent i = getIntent();
        vendor = i.getStringExtra("VENDOR");
        dslam = i.getStringExtra("DSLAM");
        model = i.getStringExtra("MODEL");
        datos = i.getStringExtra("DATOS");
        init();
    }

    private void init(){
        LinearLayout conenido = (LinearLayout) findViewById(R.id.contenido);

        LinearLayout all = new LinearLayout(this);
        all.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        all.setOrientation(LinearLayout.VERTICAL);

        all.setBackgroundResource(R.drawable.fondo3);

        LinearLayout doblepar = new LinearLayout(this);
        doblepar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        doblepar.setGravity(Gravity.CENTER_HORIZONTAL);
        doblepar.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout par = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        par.setGravity(Gravity.CENTER_HORIZONTAL);
        par.setOrientation(LinearLayout.VERTICAL);

        TextView  attr = new TextView(this);
        TextView val = new TextView(this);
        attr.setText("VENDOR");
        attr.setTextColor(Color.BLUE);
        attr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        attr.setGravity(Gravity.CENTER_HORIZONTAL);
        attr.setTypeface(Typeface.DEFAULT_BOLD);
        val.setText(vendor);
        val.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        val.setGravity(Gravity.CENTER_HORIZONTAL);
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);

        par.addView(attr);
        par.addView(val);
        par.setPadding(7, 7, 7, 7);

        doblepar.addView(par,params);

        par = new LinearLayout(this);
        par.setGravity(Gravity.CENTER_HORIZONTAL);
        par.setOrientation(LinearLayout.VERTICAL);

        attr = new TextView(this);
        val = new TextView(this);
        attr.setText("MODEL");
        attr.setTextColor(Color.BLUE);
        attr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        attr.setGravity(Gravity.CENTER_HORIZONTAL);
        attr.setTypeface(Typeface.DEFAULT_BOLD);
        val.setText(model);
        val.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        val.setGravity(Gravity.CENTER_HORIZONTAL);
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);

        par.addView(attr);
        par.addView(val);
        par.setPadding(7,7,7,7);

        doblepar.addView(par,params);
        all.addView(doblepar);
        /**PRIMERA COLUMNA   VENDOR*/
        par = new LinearLayout(this);
        par.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        par.setGravity(Gravity.CENTER_HORIZONTAL);
        par.setOrientation(LinearLayout.VERTICAL);
        par.setPadding(0, 30, 0, 25);

        attr = new TextView(this);
        val = new TextView(this);
        attr.setText("DSLAM");
        attr.setTextColor(Color.BLUE);
        attr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        attr.setGravity(Gravity.CENTER_HORIZONTAL);
        attr.setTypeface(Typeface.DEFAULT_BOLD);
        val.setText(dslam);
        val.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        val.setGravity(Gravity.CENTER_HORIZONTAL);
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);


        par.addView(attr);
        par.addView(val);
        all.addView(par);

        /**-----------------------------------**/
        /** SEGUNDA COLUMNA   DSLAM/MODEL**/


        conenido.addView(all);

        LinearLayout pElec = new LinearLayout(this);
        pElec.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pElec.setOrientation(LinearLayout.VERTICAL);

        TextView titulo = new TextView(this);
        titulo.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        titulo.setText("VALORES ELÉCTRICOS");
        titulo.setGravity(Gravity.CENTER_HORIZONTAL);
        titulo.setPadding(0,30,0,10);
        titulo.setTextColor(Color.BLUE);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);

        pElec.addView(titulo);

        LinearLayout.LayoutParams dobleCol = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        dobleCol.weight = 1;

        String[] datossplit = datos.split("&");
        int position = 0;
        for (String aDatossplit : datossplit) {

            LinearLayout tmp = new LinearLayout(this);
            tmp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tmp.setOrientation(LinearLayout.HORIZONTAL);

            if(position == 0){
                position = 1;
                tmp.setBackgroundColor(getResources().getColor(R.color.gris));
            }else{
                position = 0;
                tmp.setBackgroundColor(Color.WHITE);
            }

            String[] info = aDatossplit.split(";");
            attr = new TextView(this);
            attr.setGravity(Gravity.END);

            val = new TextView(this);
            val.setPadding(25, 0, 0, 0);

            attr.setText(info[0]);
            if(info.length == 2) {
                val.setText(info[1]);
            }else{
                val.setText("0");
            }
            val.setTextColor(getResources().getColor(R.color.celeste));
            val.setTypeface(Typeface.DEFAULT_BOLD);

            tmp.addView(attr,dobleCol);
            tmp.addView(val, dobleCol);

            pElec.addView(tmp);
        }
        conenido.addView(pElec);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        volver(null);
    }

    public void volver(View v){

        Funciones.makeBackAlert(this).show();
    }

    public void shutdown(View v){
        ArrayList<Activity> actividades = new ArrayList<>();
        actividades.add(Principal.p);
        actividades.add(VistaTopologica.topo);
        actividades.add(DCT.actividad);
        actividades.add(this);
        Funciones.makeExitAlert(this, actividades).show();
    }



}
