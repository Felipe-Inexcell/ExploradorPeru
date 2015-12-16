package cl.inexcell.exploradorperu;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import cl.inexcell.exploradorperu.objetos.Boton;
import cl.inexcell.exploradorperu.preferences.BloqueoBotones;

public class Principal extends Activity {
    private static final int REQUEST_SETTINGS_ACTION = 0;
    public static Activity p;

    private BloqueoBotones bloqueo;

    private static final String TAG = "Principal Activity";
    LocationManager locationManager;
    private EditText phone, area;
    Context mContext;

    private String asd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "INICIANDO APLICACION");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_principal);
        p = this;
        mContext = this;

        bloqueo = new BloqueoBotones(this);

        phone = (EditText) findViewById(R.id.etPpal_telefono);
        area = (EditText) findViewById(R.id.etPpal_area);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        /** Se inicia el DEMONIO **/

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                Intent service = new Intent(this, Demonio_Certificar_3G.class);
                startService(service);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "START SERVICE ERROR", Toast.LENGTH_LONG).show();
            }

        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("GPS está desactivado!")
                    .setMessage("Active GPS e reinicie la aplicación.")
                    .setPositiveButton("Ir a Configuración", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_SETTINGS_ACTION);
                        }
                    })
                    .setNeutralButton("Salir", new DialogInterface.OnClickListener() {//un listener que al pulsar, cierre la aplicacion
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Salir
                            Principal.this.finish();
                        }
                    }).show();
        }

        Botones task = new Botones(this);
        task.execute();
    }

    private class Botones extends AsyncTask<String, String, String> {
        Context context;
        ProgressDialog d;
        Boolean ok = false;
        Boton actualizar = null;

        private Botones(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            d = new ProgressDialog(context);
            d.setCancelable(false);
            d.setCanceledOnTouchOutside(false);
            d.setMessage("Cargando configuración...");
            d.show();
        }

        @Override
        protected String doInBackground(String... params) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String IMEI = telephonyManager.getDeviceId();
            String IMSI = telephonyManager.getSimSerialNumber();
            try {
                String query = SoapRequestMovistar.getButtonBlock(IMEI, IMSI);
                //String query = URLs.BLOQUEO;
                ArrayList<Boton> response = XMLParser.getButtons(query);

                bloqueo.clearAll();
                for (Boton b : response) {
                    if (b.isActualizacion())
                        actualizar = b;
                    else {
                        bloqueo.setBloqueo(b.getId(), b.isEnabled(), b.getName());
                    }
                    ok = true;
                }
                return "BLOQUEO OK";
            } catch (SAXException e) {
                e.printStackTrace();
                return "Error al leer el xml";
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                return "Error al leer el xml";
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return "Error al leer el xml";
            } catch (IOException e) {
                e.printStackTrace();
                return "Se agotó el tiempo de espera con el servidor. Compruebe su conexión a internet y reinicie la aplicación";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error no controlado:\n" + e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            if (d.isShowing()) d.dismiss();
            if (!ok) {
                Funciones.makeAlert(mContext, null, s, false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                p.finish();
                            }
                        }).show();
            } else if(actualizar != null && actualizar.isActualizacion()){
                String versionActual = getResources().getString(R.string.versioncode);
                try {
                    if (versionActual.compareTo(actualizar.getId()) < 0) {
                        AlertDialog.Builder builder = Funciones.makeAlert(mContext,
                                "Hay una nueva versión de la Aplicación",
                                "Versión Instalada: "
                                        + versionActual
                                        + "\nVersión a Instalar: " + actualizar.getId()
                                        + "\nDebe actualizar la aplicación para continuar utilizandola.",
                                false);

                        builder.setPositiveButton("Descargar e Instalar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                        /*Toast.makeText(mContext, "Descargando... si claro", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/app-debug.apk")), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        p.finish();
                        dialog.dismiss();*/
                                Update u = new Update(context, actualizar.getName());
                                u.execute();
                            }
                        });
                        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                p.finish();
                                dialog.dismiss();
                            }
                        });
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                p.finish();
                            }
                        });
                        builder.show();
                    }
                }catch (Exception e){
                    Funciones.makeAlert(mContext, e.getMessage(), e.getCause().toString(), false).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }
            }
            //Log.i("BLOQUEO", s);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_principal, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS_ACTION) {
            Log.d("SETTINGS", "CODE: " + resultCode);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "Sigue desactivado el GPS.", Toast.LENGTH_LONG).show();
                p.finish();
            }
        }
    }

    public void show_notificar_averias(View view) {
        if (bloqueo.getState("localizarAveria")) {
            Funciones.makeResultAlert(mContext, bloqueo.getMsg("localizarAveria"), false).show();
        } else {
            ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            State senal3g = conMan.getNetworkInfo(0).getState();
            State wifi = conMan.getNetworkInfo(1).getState();

            if (senal3g == State.CONNECTED || wifi == State.CONNECTED) {
                Intent i = new Intent(this, Notificar_Averias.class);
                startActivityForResult(i, 0);

            } else {
                Funciones.makeResultAlert(mContext, "No existe conexión a internet para utilizar el Programa", false).show();
            }
        }
    }

    public void buscar_cliente(View view) {
        if (bloqueo.getState("busquedaInicial")) {
            Funciones.makeResultAlert(mContext, bloqueo.getMsg("busquedaInicial"), false).show();
        } else {
            ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            State senal3g = conMan.getNetworkInfo(0).getState();
            State wifi = conMan.getNetworkInfo(1).getState();
            if (senal3g == State.CONNECTED || wifi == State.CONNECTED) {
                if (phone.getText().length() == 0 || phone.getText() == null) {
                    Funciones.makeResultAlert(mContext, "Ingrese un número de teléfono", false).show();
                    return;
                }
                if (area.getText().length() == 0 || area.getText() == null) {
                    Funciones.makeResultAlert(mContext, "Ingrese el número de área", false).show();
                    return;
                }

                Consulta_Resources c = new Consulta_Resources();
                c.execute();
            } else {
                Funciones.makeResultAlert(mContext, "No existe conexión a internet para utilizar el Programa", false).show();
            }
        }
    }


    public void show_plantas_externas(View view) {
        if (bloqueo.getState("PlantasExternas")) {
            String msg = bloqueo.getMsg("PlantasExternas");
            Funciones.makeResultAlert(mContext, msg, false).show();
        } else {
            ConnectivityManager conMan = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            State senal3g = conMan.getNetworkInfo(0).getState();
            State wifi = conMan.getNetworkInfo(1).getState();

            if (senal3g == State.CONNECTED || wifi == State.CONNECTED) {
                Intent i = new Intent(this, Plantas_Externas.class);
                startActivityForResult(i, 0);

            } else {
                Funciones.makeResultAlert(mContext, "No existe conexión a internet para utilizar el Programa", false).show();
            }
        }
    }

    public void shutdown(View view) {
        ArrayList<Activity> actividades = new ArrayList<>();
        actividades.add(p);

        Funciones.makeExitAlert(mContext, actividades).show();
    }

    public void openFAQ(View view) {
        if (bloqueo.getState("Preguntas")) {
            Funciones.makeResultAlert(mContext, bloqueo.getMsg("Preguntas"), false).show();
        } else {
            startActivity(new Intent(this, FAQActivity.class));
        }
    }


    private class Consulta_Resources extends AsyncTask<String, Integer, String> {

        private final ProgressDialog dialog = new ProgressDialog(Principal.this);
        private int code;
        String errorMessage = "";

        protected void onPreExecute() {
            this.dialog.setMessage("Buscando Cliente...\nPaciencia, esto puede tardar.");
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setCancelable(false);
            this.dialog.show();
            //super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            String respuesta = null;

            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEI = telephonyManager.getDeviceId();
                String IMSI = telephonyManager.getSimSerialNumber();
                String consulta;
                if (phone.getText().toString().equals("2") && area.getText().toString().equals("2")) {
                    consulta = URLs.RESOURCE;
                } else
                    consulta = SoapRequestMovistar.getResource(IMEI, IMSI, area.getText().toString(), phone.getText().toString());

                ArrayList<String> retorno = XMLParser.getReturnCode(consulta);

                code = Integer.valueOf(retorno.get(0));

                if (code == 0) {
                    respuesta = consulta;
                    Log.i(TAG, retorno.get(1));
                } else
                    respuesta = retorno.get(1);
                return respuesta;

            } catch (HttpHostConnectException e2) {
                errorMessage = "Se agotó el tiempo de espera. Por favor reintente";
                return null;
            } catch (HttpResponseException e3) {
                e3.printStackTrace();
                errorMessage = "Se agotó el tiempo de espera. Por favor reintente";
                return null;
            } catch (ParseException p) {
                p.printStackTrace();
                errorMessage = "Error en la recepción de los datos. Por favor reintente";
                return null;
            } catch (SocketTimeoutException s1) {
                s1.printStackTrace();
                errorMessage = "Se agotó el tiempo de espera. Por favor reintente";
                return null;
            } catch (ConnectTimeoutException et) {
                et.printStackTrace();
                errorMessage = "Se agotó el tiempo de espera. Por favor reintente";
                return null;
            } catch (Exception e1) {
                e1.printStackTrace();
                errorMessage = "Ha ocurrido un error con la respuesta del servidor.";
                return null;
            }
        }


        protected void onPostExecute(String result) {
            if (result != null) {
                if (code == 0) {
                    Intent topologica = new Intent(getApplicationContext(), VistaTopologica.class);
                    topologica.putExtra("AREA", area.getText().toString());
                    topologica.putExtra("PHONE", phone.getText().toString());
                    topologica.putExtra("RESULT", result);
                    startActivityForResult(topologica, 0);
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(50);
                } else {
                    Funciones.makeResultAlert(mContext, result, false).show();
                }
            } else {
                Funciones.makeResultAlert(mContext, errorMessage, false).show();
            }

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }
    }

    private class Update extends AsyncTask<String, String, File> {
        Context ctx;
        ProgressDialog d;
        String url;

        private Update(Context ctx, String URL) {
            this.ctx = ctx;
            this.url = URL;
        }

        @Override
        protected void onPreExecute() {
            d = new ProgressDialog(ctx);
            d.setCancelable(false);
            d.setCanceledOnTouchOutside(false);
            d.setMessage("Descargando nueva versión...");
            d.show();
        }

        @Override
        protected File doInBackground(String... params) {
            return Funciones.Update(url);
        }

        @Override
        protected void onPostExecute(File file) {
            if (d.isShowing()) d.dismiss();
            if (file != null) {
                //Toast.makeText(ctx, file, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
            p.finish();
        }
    }


}
