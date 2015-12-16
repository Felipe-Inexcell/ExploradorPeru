package cl.inexcell.exploradorperu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;


public class CasaCerrada extends FragmentActivity implements GoogleMap.OnMapLoadedCallback, GoogleMap.OnMyLocationChangeListener, View.OnClickListener {
    private GoogleMap mapa;
    private final String TAG = "Plantas_Externas";
    Context mContext;
    LinearLayout mapStatus;
    TextView mapStatusText;
    EditText description;
    Activity p;
    String Phone;

    Button tomarFoto, verFoto, enviar;

    boolean gpsOk = false;

    private Bitmap b = null;
    private static int TAKE_PICTURE = 1;
    private String name = Environment.getExternalStorageDirectory() + "/casacerrada.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_casa_cerrada);
        mContext = this;
        p=this;

        Phone = getIntent().getStringExtra("PHONE");

        description = (EditText) findViewById(R.id.description);
        tomarFoto = (Button) findViewById(R.id.tomarfoto);
        verFoto = (Button) findViewById(R.id.verfoto);
        enviar = (Button) findViewById(R.id.enviar);
        tomarFoto.setOnClickListener(this);
        verFoto.setOnClickListener(this);
        enviar.setOnClickListener(this);
        verFoto.setEnabled(false);
        mapStatus = (LinearLayout) findViewById(R.id.mapStatus);

        mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment)).getMap();
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.setMyLocationEnabled(true);
        mapa.getUiSettings().setMyLocationButtonEnabled(true);
        mapa.getUiSettings().setCompassEnabled(true);
        mapa.getUiSettings().setZoomControlsEnabled(true);

        mapa.setOnMapLoadedCallback(this);

    }


    @Override
    public void onMyLocationChange(Location location) {
        if (location != null && !gpsOk) {
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapa.getMyLocation().getLatitude(), mapa.getMyLocation().getLongitude()), 15));
            mapStatus.setVisibility(View.GONE);
            gpsOk = true;
        }
    }

    @Override
    public void onMapLoaded() {
        if (mapa.getMyLocation() != null) {
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapa.getMyLocation().getLatitude(), mapa.getMyLocation().getLongitude()), 15));
            mapStatus.setVisibility(View.GONE);
            gpsOk = true;
        }

    }

    public void shutdown(View v) {
        ArrayList<Activity> actividades = new ArrayList<>();
        actividades.add(Principal.p);
        actividades.add(FactActivity.p);
        actividades.add(VistaTopologica.topo);
        actividades.add(this);
        Funciones.makeExitAlert(this, actividades).show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        volver(null);
    }

    public void volver(View view) {
        InputMethodManager imm = (InputMethodManager) p.getSystemService(Context.INPUT_METHOD_SERVICE);
        View foco = p.getCurrentFocus();
        if (foco == null || !imm.hideSoftInputFromWindow(foco.getWindowToken(), 0)) {
            Funciones.makeBackAlert(mContext).show();
        }
    }

    /**
     * Boton Camara *
     */
    public void capturarImagen() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri output = Uri.fromFile(new File(name));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        startActivityForResult(intent, TAKE_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE) {
            if (data != null) {
                if (data.hasExtra("data")) {
                    b = (Bitmap) data.getParcelableExtra("data");
                }
            } else {
                b = BitmapFactory.decodeFile(name);

            }
        }
        try {
            b = Bitmap.createScaledBitmap(b, 640, 480, true);
        } catch (Exception ex) {
        }

        verFoto.setEnabled(true);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tomarfoto) {
            capturarImagen();
        }
        if (v.getId() == R.id.verfoto) {
            AlertDialog.Builder bi = new AlertDialog.Builder(this);
            bi.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            ImageView iv = new ImageView(this);
            iv.setImageBitmap(b);

            bi.setView(iv);
            bi.show();
        }
        if (v.getId() == R.id.enviar) {
            if (b == null) {
                Funciones.makeResultAlert(mContext, "Debe tomar una fotografía del lugar", false).show();
                return;
            }

            if (description.getText().length() == 0) {
                Funciones.makeResultAlert(mContext, "Debe ingresar una observación", false).show();
                return;
            }

            EnviarTask t = new EnviarTask(this);
            t.execute(description.getText().toString(),Funciones.encodeTobase64(b), String.valueOf(mapa.getMyLocation().getLatitude()), String.valueOf(mapa.getMyLocation().getLongitude()));
        }
    }

    private class EnviarTask extends AsyncTask<String, String, String> {
        Context tContext;
        ProgressDialog d;
        boolean isOk = false;

        private EnviarTask(Context tContext) {
            this.tContext = tContext;
        }

        @Override
        protected void onPreExecute() {
            d = new ProgressDialog(tContext);
            d.setCanceledOnTouchOutside(false);
            d.setCancelable(false);
            d.setMessage("Enviando información...");
            d.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEI = telephonyManager.getDeviceId();
                String IMSI = telephonyManager.getSimSerialNumber();
                String query;
                if(Phone.equals("3")){
                    query = URLs.CASACERRADA;
                }else{
                    query = SoapRequestMovistar.sendClosedHouse(IMEI,IMSI,params[0],params[1],params[2],params[3], Phone);
                }

                ArrayList<String> retorno = XMLParser.getReturnCode(query);

                isOk = retorno.get(0).equals("0");
                return retorno.get(1);
            } catch (IOException e) {
                e.printStackTrace();
                return "Error al consultar con el servicio.";
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                return "Error al leer el XML";
            } catch (SAXException e) {
                e.printStackTrace();
                return "Error al leer el XML";
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return "Error al leer el XML";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Funciones.makeAlert(mContext, null, s,false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isOk) {
                                ((Activity) tContext).finish();
                                if (FactActivity.p != null)
                                    FactActivity.p.finish();

                            }
                        }
                    }).show();

            if(d.isShowing())d.dismiss();
        }
    }
}
