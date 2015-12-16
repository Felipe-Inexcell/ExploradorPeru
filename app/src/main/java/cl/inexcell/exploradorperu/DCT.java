package cl.inexcell.exploradorperu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.conn.ConnectTimeoutException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import cl.inexcell.exploradorperu.objetos.DCTParamCaja;
import cl.inexcell.exploradorperu.objetos.DCTParamFono;
import cl.inexcell.exploradorperu.objetos.DCTParamFonoCabecera;


public class DCT extends Activity {
    public static Activity actividad;
    Context mContext;
    String Phone;
    String Area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dct);
        actividad = this;
        mContext = this;
        Phone = getIntent().getStringExtra("PHONE");
        Area = getIntent().getStringExtra("AREA");

        ObtenerDCT task = new ObtenerDCT();
        task.execute();

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        volver(null);
    }

    public void volver(View v) {

        Funciones.makeBackAlert(this).show();
    }

    public void shutdown(View v) {
        ArrayList<Activity> actividades = new ArrayList<>();
        actividades.add(Principal.p);
        actividades.add(VistaTopologica.topo);
        actividades.add(this);
        Funciones.makeExitAlert(this, actividades).show();
    }

    private class ObtenerDCT extends AsyncTask<String, String, String> {
        ProgressDialog dialog;

        boolean stateOk = false;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
            dialog.setMessage("Buscando información...\n" +
                    "Este proceso podría tardar unos segundos.");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String query;
                if (Phone.equals("2")) {
                    query = URLs.PARAELECTRI;
                } else {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String IMEI = telephonyManager.getDeviceId();
                    String IMSI = telephonyManager.getSimSerialNumber();
                    query = SoapRequestMovistar.getParaElectri(IMEI, IMSI, Area, Phone);
                }

                ArrayList<String> retorno = XMLParser.getReturnCode(query);
                if (retorno.get(0).compareTo("0") == 0) {
                    stateOk = true;
                    return query;
                } else {
                    return retorno.get(1);
                }
            } catch (ConnectTimeoutException e) {
                e.printStackTrace();
                return "Se agoto el tiempo de espera. Por favor reintente";
            } catch (SAXException e) {
                e.printStackTrace();
                return "Error al leer el XML";
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                return "Error al leer el XML";
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return "Error al leer el XML";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error en la conexión con el servidor. Por favor reintente";
            } catch (Exception e) {
                return "Error no controlado. Causa: "+e.getCause();
            }
        }

        @Override
        protected void onPostExecute(String s) {

            if (stateOk) {
                try {
                    cl.inexcell.exploradorperu.objetos.DCT response = XMLParser.getDCTinfo(s);
                    dibujar(response);
                } catch (Exception e) {

                    Funciones.makeAlert(actividad, null, e.getMessage(),false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Activity) mContext).finish();
                        }
                    }).show();
                }
            } else {
                Funciones.makeAlert(actividad, null, s,false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity) mContext).finish();
                    }
                }).show();
            }
            if (dialog.isShowing()) dialog.dismiss();
        }
    }

    private void dibujar(cl.inexcell.exploradorperu.objetos.DCT response) {
        LinearLayout.LayoutParams paramsCenterTextIzq = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsCenterTextDer = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsCenterTextIzq.weight = 2;
        paramsCenterTextDer.weight = 1;

        LinearLayout cCajas = (LinearLayout) findViewById(R.id.contenido);
        TextView t = Funciones.makeTextView(this, "Tecnologías por Fabricante", 1);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        cCajas.addView(t);

        LinearLayout contCajas = new LinearLayout(this);
        contCajas.setBackgroundResource(R.drawable.fondo3);
        contCajas.setOrientation(LinearLayout.VERTICAL);
        contCajas.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout contenido = new LinearLayout(this);
        contenido.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contenido.setBackgroundResource(R.color.celeste);
        contenido.setOrientation(LinearLayout.HORIZONTAL);

        TextView text = new TextView(this);
        text.setText("Fabricante");
        text.setTextColor(getResources().getColor(android.R.color.white));
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        contenido.addView(text, paramsCenterTextIzq);

        text = new TextView(this);
        text.setText("Velocidad");
        text.setTextColor(getResources().getColor(android.R.color.white));
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        contenido.addView(text, paramsCenterTextDer);
        contCajas.addView(contenido);

        int id = -1;
        for (DCTParamCaja cajas : response.getParametrosCajas()) {
            if (cajas.getId() > id) {
                id = cajas.getId();
                contenido = new LinearLayout(this);
                contenido.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                contenido.setBackgroundResource(android.R.color.holo_green_light);
                contenido.setOrientation(LinearLayout.HORIZONTAL);
                text = new TextView(this);
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                text.setText(cajas.getProveedor());
                contenido.addView(text);
                contCajas.addView(contenido);
            }

            contenido = new LinearLayout(this);
            contenido.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            contenido.setOrientation(LinearLayout.HORIZONTAL);
            text = new TextView(this);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setText(cajas.getDslam());
            contenido.addView(text, paramsCenterTextIzq);

            text = new TextView(this);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setText(cajas.getVelocidad());
            contenido.addView(text, paramsCenterTextDer);

            if (response.getParametrosCajas().indexOf(cajas) == 0 || response.getParametrosCajas().indexOf(cajas) % 2 == 0) {
                contenido.setBackgroundResource(R.color.gris);
            }
            contCajas.addView(contenido);
        }
        cCajas.addView(contCajas);

        TextView titulo = Funciones.makeTextView(this, "Caja Terminal en Poste", 1);
        titulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titulo.setPadding(0, 20, 0, 0);
        cCajas.addView(titulo);

        LinearLayout.LayoutParams paramsPar = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsFono = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsTipo = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams paramsPerfil = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        paramsPar.weight = 2;
        paramsFono.weight = 3;
        paramsTipo.weight = 3;
        paramsPerfil.weight = 4;

        contCajas = new LinearLayout(this);
        contCajas.setBackgroundResource(R.drawable.fondo3);
        contCajas.setOrientation(LinearLayout.VERTICAL);
        contCajas.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        contenido = new LinearLayout(this);
        contenido.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contenido.setBackgroundResource(R.color.celeste);
        contenido.setOrientation(LinearLayout.HORIZONTAL);

        text = new TextView(this);
        text.setText("Par");
        text.setTextColor(getResources().getColor(android.R.color.white));
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        contenido.addView(text, paramsPar);

        text = new TextView(this);
        text.setText("Telefono");
        text.setTextColor(getResources().getColor(android.R.color.white));
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        contenido.addView(text, paramsFono);

        text = new TextView(this);
        text.setText("Tipo");
        text.setTextColor(getResources().getColor(android.R.color.white));
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        contenido.addView(text, paramsTipo);

        text = new TextView(this);
        text.setText("Perfil");
        text.setTextColor(getResources().getColor(android.R.color.white));
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        contenido.addView(text, paramsPerfil);

        contCajas.addView(contenido);
        LinearLayout separador = new LinearLayout(this);
        separador.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        separador.setBackgroundResource(android.R.color.black);
        contCajas.addView(separador);
        for (final DCTParamFono fonos : response.getParametrosFonos()) {

            contenido = new LinearLayout(this);
            contenido.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            contenido.setOrientation(LinearLayout.HORIZONTAL);

            text = new TextView(this);
            text.setPadding(0, 5, 0, 5);
            text.setText(fonos.getPar());
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            contenido.addView(text, paramsPar);

            text = new TextView(this);
            text.setPadding(0, 5, 0, 5);
            text.setText(fonos.getArea() + "" + fonos.getFono());
            text.setBackgroundResource(R.drawable.boton_style3);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fonos.getTipo().compareTo("Banda Ancha") != 0) {
                        return;
                    }
                    DCTParamFonoCabecera cab = fonos.getParametrosFonosCabecera().get(0);
                    String datoselectricos = "";

                        for (int i = 0; i < cab.getParametrosElectricos().size(); i++) {
                            if (i == 0) {
                                datoselectricos += cab.getParametrosElectricos().get(i);
                            } else {

                                datoselectricos += "&" + cab.getParametrosElectricos().get(i);
                            }
                        }

                        Intent intent = new Intent(mContext, DetalleElectrico.class);
                        intent.putExtra("VENDOR", cab.getVendor());
                        intent.putExtra("DSLAM", cab.getDslam());
                        intent.putExtra("MODEL", cab.getModel());
                        intent.putExtra("DATOS", datoselectricos);
                        startActivity(intent);

                }
            });
            contenido.addView(text, paramsFono);

            text = new TextView(this);
            text.setPadding(0, 5, 0, 5);
            try {
                String test = new String(fonos.getTipo().getBytes(Xml.Encoding.ISO_8859_1.name()), Xml.Encoding.UTF_8.name());
                text.setText(test);
            }catch (UnsupportedEncodingException e) {
                e.printStackTrace();

                text.setText(fonos.getTipo());

            }
            //text.setText(fonos.getTipo());
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            contenido.addView(text, paramsTipo);

            text = new TextView(this);
            text.setPadding(0, 5, 0, 5);
            text.setText(fonos.getPerfil());
            text.setTextColor(getResources().getColor(R.color.orange));
            text.setBackgroundResource(R.color.gris);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            contenido.addView(text, paramsPerfil);

            contCajas.addView(contenido);
            separador = new LinearLayout(this);
            separador.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            separador.setBackgroundResource(android.R.color.black);
            contCajas.addView(separador);
        }
        cCajas.addView(contCajas);
    }

}
