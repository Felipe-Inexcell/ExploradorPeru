package cl.inexcell.exploradorperu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.conn.ConnectTimeoutException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import cl.inexcell.exploradorperu.objetos.Boton;
import cl.inexcell.exploradorperu.preferences.BloqueoBotones;

public class VistaTopologica extends Activity {
    private String TAG = "TOPOLOGICA";
    public static Activity topo;
    private Context mContext;
    private boolean isTV = false;

    private BloqueoBotones bloqueo;

    int idButton = R.layout.layoutbuttontopologica;
    int contentlayout = R.layout.contenidolayout;
    int linea = R.layout.layouttextotexto;
    ArrayList<String> decos_reg;
    ArrayList<String> datos_cliente_reg;

    //TODO: Declaraciones
    int left;
    private ArrayList<Integer> nodesIn;
    private ConnectivityManager conMan;
    private String output;
    private LinearLayout LContenido;
    private ArrayList<String> pares;
    private ArrayList<Integer> ids_botones;
    private ArrayList<Integer> ids_contenidos;
    private String Area;
    private String Phone;
    private String parActual;
    private EditText valor;
    private String tipo;
    private int idCajaButton;
    private int idCajaContent;
    private int tipoProcedimiento;
    private Button btnCertifica, btnFact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity sin parte superior
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_topologica);
        topo = this;
        bloqueo = new BloqueoBotones(this);
        mContext = this;
        Phone = getIntent().getStringExtra("PHONE");
        Area = getIntent().getStringExtra("AREA");
        conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        output = getIntent().getStringExtra("RESULT");
        LContenido = (LinearLayout) findViewById(R.id.contenido_topologica1);
        valor = (EditText) findViewById(R.id.editText11);
        decos_reg = new ArrayList<>();
        datos_cliente_reg = new ArrayList<>();

        Log.i(TAG, Phone);

        init();

        getStateButtons();

        Ordenar_informacion tarea1 = new Ordenar_informacion();
        tarea1.execute();
    }

    private void getStateButtons() {
        try {
            ArrayList<String> states = XMLParser.getStateButtonsTopologica(output);

            if (states.get(0).equals("false")) {
                btnCertifica.setVisibility(View.GONE);
            }
            if (states.get(1).equals("false")) {
                btnFact.setVisibility(View.GONE);

            }

        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            e.printStackTrace();

            Funciones.makeResultAlert(mContext, "Error al leer la respuesta del servidor.", false).show();
        } catch (IOException e) {
            e.printStackTrace();
            Funciones.makeResultAlert(mContext, "Se agotó el tiempo de conexión. Por favor reintente", false).show();
        }

    }

    public void shutdown(View v) {
        ArrayList<Activity> actividades = new ArrayList<>();
        actividades.add(Principal.p);
        actividades.add(topo);
        Funciones.makeExitAlert(this, actividades).show();
    }


    public void consultar(View v) {
        if (bloqueo.getState("Certifica")) {
            Funciones.makeResultAlert(mContext, bloqueo.getMsg("Certifica"), false).show();
        } else {
            State state3g = conMan.getNetworkInfo(0).getState();
            State stateWifi = conMan.getNetworkInfo(1).getState();
            if (state3g == State.CONNECTED || stateWifi == State.CONNECTED) {
                Intent certificar = new Intent(this, Certificar.class);
                certificar.putExtra("AREA", Area);
                certificar.putExtra("PHONE", Phone);
                startActivity(certificar);
            } else {
                Funciones.makeResultAlert(mContext, "Error: \nNo hay conexión a internet", false).show();
            }
        }
    }

    public void certificacionTipo(int tipo) {
        Intent certificar = new Intent(this, Certificar.class);
        certificar.putExtra("PHONE", Phone);
        startActivity(certificar);
    }

    @Override
    public void onBackPressed() {
        volver(null);
    }

    public void volver(View view) {
        Funciones.makeBackAlert(this).show();
    }

    public void init() {
        btnCertifica = (Button) this.findViewById(R.id.buttonCertifica);
        btnFact = (Button) this.findViewById(R.id.buttonFATC);
        try {
            // TODO: condicion procedimiento 1
            //tipoProcedimiento = 1;
            tipoProcedimiento = Integer.parseInt(XMLParser.getOperationId(output));
            Log.d(TAG, "opID= " + tipoProcedimiento);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    //TODO: Ordenar_Informacion
    private class Ordenar_informacion extends AsyncTask<String, Integer, ArrayList<Bundle>> {

        private final ProgressDialog dialog = new ProgressDialog(VistaTopologica.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Consultando Información Cliente...");
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setCancelable(false);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    Funciones.makeResultAlert(mContext, "Operación Interrumpida", false).show();
                }
            });
            this.dialog.show();
            //super.onPreExecute();
        }

        protected ArrayList<Bundle> doInBackground(String... params) {

            ArrayList<Bundle> todo;
            try {
                Log.i(TAG, output);
                todo = XMLParser.getResourcesNew(output);

            } catch (Exception e1) {
                e1.printStackTrace();
                todo = null;
            }

            return todo;
        }


        protected void onPostExecute(ArrayList<Bundle> result) {

            ids_botones = new ArrayList<>();
            ids_contenidos = new ArrayList<>();
            nodesIn = new ArrayList<>();
            if (result == null) {
                Funciones.makeAlert(mContext, null, "Error al leer el XML", false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) mContext).finish();
                            }
                        }).show();
            } else {
                for (Bundle b : result) {


                    String swit = b.getString("TYPE");
                    Drawable left = null;
                    if (swit.equals("DIRECCION")) {
                        Log.e(TAG, "DIRECCION");
                        left = getResources().getDrawable(R.drawable.cc_direct1);
                    }
                    if (swit.equals("PRODUCTOS")) {
                        Log.e(TAG, "PRODUCTOS");
                        left = getResources().getDrawable(R.drawable.vt_prod2);
                    }
                    if (swit.equals("PLANTA INTERNA")) {
                        Log.e(TAG, "PLANTA INTERNA");
                        left = getResources().getDrawable(R.drawable.vt_planta1);
                    }
                    if (swit.equals("PLANTA EXTERNA")) {
                        Log.e(TAG, "PLANTA EXTERNA");
                        left = getResources().getDrawable(R.drawable.vt_pe1);
                    }
                    if (swit.equals("MASIVAS")) {
                        Log.e(TAG, "MASIVAS");
                        left = getResources().getDrawable(R.drawable.vt_masiva);
                    }
                    if (b.getString("VALUE").equals("SERVICIO TELEFONIA")) {
                        Log.e(TAG, "SERVICIO TELEFONIA");
                        left = getResources().getDrawable(R.drawable.vt_stb1);
                    }
                    if (b.getString("VALUE").equals("SERVICIO BANDA ANCHA")) {
                        Log.e(TAG, "SERVICIO BANDA ANCHA");
                        left = getResources().getDrawable(R.drawable.vt_baf1);
                    }
                    if (b.getString("VALUE").equals("SERVICIO TELEVISION")) {
                        Log.e(TAG, "SERVICIO TELEVISION");

                        left = getResources().getDrawable(R.drawable.vt_dth2);
                    }
                    if (swit.equals("CAJA")) {
                        Log.e(TAG, "CAJA");
                        left = getResources().getDrawable(R.drawable.vt_caja1);
                    }

                    String newId = "Boton" + b.getString("TYPE") + b.getString("VALUE");
                    String newIdCont = "Contenido" + b.getString("TYPE") + b.getString("VALUE");
                    Log.d(TAG, newId);

                    LinearLayout linearLayout;
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    LayoutInflater inflater2 = LayoutInflater.from(getApplicationContext());
                    final LinearLayout contlay = (LinearLayout) inflater2.inflate(contentlayout, null, false);

                    /** CREAMOS BOTON Y LO AñADIMOS **/
                    LinearLayout linearLayoutBoton = (LinearLayout) inflater.inflate(idButton, null, false);
                    Button boton = (Button) linearLayoutBoton.findViewById(R.id.button1);
                    boton.setText(b.getString("VALUE"));
                    int ic_buttom1 = R.drawable.ic_bottom1;
                    //boton.setCompoundDrawablesWithIntrinsicBounds(left,0, ic_buttom1, 0);
                    boton.setId(str2int(newId));

                    final ArrayList<String> botones = b.getStringArrayList("BOTONES");

                    ArrayList<String> datos = b.getStringArrayList("IDENTIFICATION");
                    String valor = "";
                    if (datos != null) {
                        for (String d : datos) {
                            String decos_linea = "";
                            Boolean isButton = false;
                            final String[] lineas = d.split(";");
                            for (int k = 0; k < lineas.length; k++) {
                                String[] informacion = lineas[k].split("&");
                                linearLayout = (LinearLayout) inflater.inflate(linea, null, false);

                                TextView izq = (TextView) linearLayout.findViewById(R.id.textView1);
                                izq.setTextColor(getResources().getColor(R.color.black));
                                valor = b.getString("VALUE");
                                if (valor.equals("SERVICIO TELEVISION")) {
                                    Boton button = getButton(botones, "Lapiz");

                                    if (!isButton && button != null && button.isEnabled()) {
                                        ImageButton ed = (ImageButton) linearLayout.findViewById(R.id.imageButton1);
                                        ed.setVisibility(View.VISIBLE);
                                        ed.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                final CharSequence[] options, actions;

                                                options = new CharSequence[]{"REEMPLAZAR DECO"};
                                                actions = new CharSequence[]{"ACTION103"};

                                            /*if (tipoProcedimiento == 2) {
                                                options = new CharSequence[]{"REEMPLAZAR DECO"};
                                                //actions = new CharSequence[]{"ACTION101","ACTION102","ACTION103"};
                                                actions = new CharSequence[]{"ACTION103"};
                                                //options = new CharSequence[]{"REACTIVAR DECO", "REEMPLAZAR DECO"};
                                                //options = new CharSequence[]{};
                                                //actions = new CharSequence[]{"ACTION102","ACTION103"};
                                                //actions = new CharSequence[]{};
                                            } else if (tipoProcedimiento == 1) {
                                                //options = new CharSequence[]{"ACTIVAR DECO", "REACTIVAR DECO", "REEMPLAZAR DECO"};

                                                options = new CharSequence[]{"REEMPLAZAR DECO"};
                                                //actions = new CharSequence[]{"ACTION101","ACTION102","ACTION103"};
                                                actions = new CharSequence[]{"ACTION103"};
                                            } else {
                                                return;
                                            }*/

                                                AlertDialog.Builder dialog = new AlertDialog.Builder(VistaTopologica.topo);
                                                dialog.setTitle("Selecione una opción:");
                                                dialog.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, final int which) {
                                                        final String serieantiguaDECO = lineas[1].split("&")[1];
                                                        final String serieantiguaTARJETA = lineas[2].split("&")[1];

                                                        if (actions[which].equals("ACTION101")) {
                                                            Log.w(TAG, "ACTION101--TYPE=" + serieantiguaDECO + ";" + serieantiguaTARJETA);
                                                            ActionButtonTask ab = new ActionButtonTask(Area, Phone, serieantiguaDECO + ";" + serieantiguaTARJETA, actions[which].toString());
                                                            ab.execute();
                                                        }

                                                        if (actions[which].equals("ACTION102")) {
                                                            Log.w(TAG, "ACTION102--TYPE=" + serieantiguaDECO + ";" + serieantiguaTARJETA);
                                                            ActionButtonTask ab = new ActionButtonTask(Area, Phone, serieantiguaDECO + ";" + serieantiguaTARJETA, actions[which].toString());
                                                            ab.execute();
                                                        }

                                                        if (actions[which].equals("ACTION103")) {
                                                            final Dialog dial = new Dialog(VistaTopologica.topo);
                                                            dial.setContentView(R.layout.new_deco_view);
                                                            final EditText serieDeco = (EditText) dial.findViewById(R.id.editText);
                                                            final EditText serieTarjeta = (EditText) dial.findViewById(R.id.editText2);
                                                            ImageButton ok = (ImageButton) dial.findViewById(R.id.bOK);
                                                            ImageButton nok = (ImageButton) dial.findViewById(R.id.bNOK);
                                                            dial.setTitle("Ingrese datos nuevo DECO:");
                                                            ok.setOnClickListener(new OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    if (serieDeco.getText().toString().length() == 10
                                                                            && serieTarjeta.getText().toString().length() == 10) {
                                                                        tipo = serieDeco.getText().toString()
                                                                                + ";" + serieTarjeta.getText().toString()
                                                                                + "-" + serieantiguaDECO
                                                                                + ";" + serieantiguaTARJETA;
                                                                        Log.w(TAG, "ACTION103--TYPE=" + tipo);
                                                                        ActionButtonTask ab = new ActionButtonTask(Area, Phone, tipo, actions[which].toString());
                                                                        ab.execute();
                                                                        dial.dismiss();
                                                                    } else {
                                                                        Funciones.makeResultAlert(mContext, "Debe ingresar ambos números de serie de 10 digitos.", false).show();
                                                                    }
                                                                }
                                                            });
                                                            nok.setOnClickListener(new OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    dial.dismiss();
                                                                }
                                                            });

                                                            dial.show();
                                                        }

                                                        if (actions[which].equals("ACTION402")) {
                                                            //final String serieantiguaDECO = lineas[1].split("&")[1];
                                                            //final String serieantiguaTARJETA = lineas[2].split("&")[1];
                                                            final Dialog dial = new Dialog(VistaTopologica.topo);
                                                            dial.setContentView(R.layout.new_deco_view);
                                                            final EditText serieDeco = (EditText) dial.findViewById(R.id.editText);
                                                            final EditText serieTarjeta = (EditText) dial.findViewById(R.id.editText2);
                                                            serieDeco.setText(serieantiguaDECO);
                                                            serieTarjeta.setText(serieantiguaTARJETA);
                                                            serieDeco.setEnabled(false);
                                                            serieTarjeta.setEnabled(false);
                                                            ImageButton ok = (ImageButton) dial.findViewById(R.id.bOK);
                                                            ImageButton nok = (ImageButton) dial.findViewById(R.id.bNOK);
                                                            dial.setTitle("Eliminar DECO:");
                                                            ok.setOnClickListener(new OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    tipo = serieantiguaDECO
                                                                            + ";" + serieantiguaTARJETA;
                                                                    Log.w(TAG, "ACTION402--TYPE=" + tipo);
                                                                    ActionButtonTask ab = new ActionButtonTask(Area, Phone, tipo, "ACTION402");
                                                                    ab.execute();
                                                                    dial.dismiss();
                                                                }
                                                            });
                                                            nok.setOnClickListener(new OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    dial.dismiss();
                                                                }
                                                            });

                                                            dial.show();
                                                        }
                                                    }
                                                });
                                                dialog.setCancelable(false);
                                                dialog.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {


                                                    }
                                                });
                                                dialog.show();
                                            }
                                        });
                                        isButton = true;
                                    }
                                }
                                //--------------


                                //------


                                if (valor.equals("SERVICIO BANDA ANCHA")) {
                                    Boton button = getButton(botones, "resetDslam");

                                    if (!isButton && button != null && button.isEnabled()) {
                                        ImageButton ed = (ImageButton) linearLayout.findViewById(R.id.imageButton1);
                                        ed.setVisibility(View.VISIBLE);
                                        ed.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final CharSequence[] options, actions;
                                                options = new CharSequence[]{"RS", "CONSULTAR ESTADO"};
                                                actions = new CharSequence[]{"ACTION201", "ACTION202"};

                                                AlertDialog.Builder dialog = new AlertDialog.Builder(VistaTopologica.topo);
                                                dialog.setTitle("Selecione una opción:");
                                                dialog.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, final int which) {
                                                        if (actions[which].equals("ACTION201")) {
                                                            //Log.w(TAG,"ACTION104--TYPE="+serieantiguaDECO+";"+serieantiguaTARJETA);
                                                            ActionButtonTask ab = new ActionButtonTask(Area, Phone, "", actions[which].toString());
                                                            ab.execute();
                                                        }
                                                        if (actions[which].equals("ACTION202")) {
                                                            //Log.w(TAG,"ACTION104--TYPE="+serieantiguaDECO+";"+serieantiguaTARJETA);
                                                            ActionButtonTask ab = new ActionButtonTask(Area, Phone, "", actions[which].toString());
                                                            ab.execute();
                                                        }
                                                    }
                                                });
                                                dialog.setCancelable(false);
                                                dialog.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {


                                                    }
                                                });
                                                dialog.show();
                                            }
                                        });
                                        isButton = true;
                                    }
                                }


                                TextView der = (TextView) linearLayout.findViewById(R.id.textView2);
                                der.setTextColor(getResources().getColor(R.color.celeste));
                                if (k == 0 && b.getString("VALUE").equals("SERVICIO TELEVISION")) {
                                    der.setBackgroundColor(Color.BLUE);
                                    izq.setBackgroundColor(Color.BLUE);
                                    der.setTextColor(Color.WHITE);
                                    izq.setTextColor(Color.WHITE);
                                }

                                izq.setText(" " + informacion[0] + ":  ");
                                if (informacion.length == 1)
                                    der.setText("---");
                                else
                                    der.setText(informacion[1]);
                                if (valor.equals("SERVICIO TELEVISION") && !isTV) {
                                    Boton buttonAdd = getButton(botones, "AgegarDeco");
                                    Boton buttonReac = getButton(botones, "ReactivarDeco");

                                    if (buttonAdd != null && buttonAdd.isEnabled())
                                        contlay.addView(putButtonAddTV(buttonAdd.getName()));
                                    if (buttonReac != null && buttonReac.isEnabled())
                                        contlay.addView(putButtonReactivateTV(buttonReac.getName()));
                                }

                                if (valor.equals("SERVICIO TELEVISION")) {
                                    String p = informacion[0] + ";" + informacion[1];
                                    if (decos_linea.compareTo("") == 0) {
                                        decos_linea += p;
                                    } else {
                                        decos_linea += "&" + p;
                                    }
                                    if (k == (lineas.length - 1))
                                        decos_reg.add(decos_linea);
                                }
                                if (swit.equals("DIRECCION")) {
                                    String p = informacion[0] + ";" + informacion[1];
                                    if (decos_linea.compareTo("") == 0) {
                                        decos_linea += p;
                                    } else {
                                        decos_linea += "&" + p;
                                    }
                                    if (k == (lineas.length - 1))
                                        datos_cliente_reg.add(decos_linea);
                                }


                                contlay.addView(linearLayout);
                            }


                        }

                    } else {
                        if (b.getString("VALUE").equals("SERVICIO TELEVISION") && !isTV) {
                            Boton buttonAdd = getButton(botones, "AgegarDeco");
                            Boton buttonReac = getButton(botones, "ReactivarDeco");

                            if (buttonAdd != null && buttonAdd.isEnabled())
                                contlay.addView(putButtonAddTV(buttonAdd.getName()));
                            if (buttonReac != null && buttonReac.isEnabled())
                                contlay.addView(putButtonReactivateTV(buttonReac.getName()));
                        } else if (swit.equals("CAJA")) {
                            idCajaButton = boton.getId();
                            idCajaContent = str2int(newIdCont);
                        } else
                            continue;
                    }


                    contlay.setVisibility(View.GONE);
                    boton.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Log.d(TAG, arg0.getId() + "");
                            Log.d(TAG, "contenido correpondiente: " + ids_contenidos.get(ids_botones.indexOf(arg0.getId())));
                            ArrayList<Button> bs = new ArrayList<>();
                            ArrayList<View> ls = new ArrayList<>();

                            int posicion = ids_botones.indexOf(arg0.getId());
                            for (int i = 0; i < ids_botones.size(); i++) {
                                if (i != posicion) {
                                    Button b = (Button) LContenido.findViewById(ids_botones.get(i));
                                    View v = LContenido.findViewById(ids_contenidos.get(i));
                                    bs.add(b);
                                    ls.add(v);

                                }

                            }

                            if (contlay.getVisibility() == View.GONE) {
                                contlay.setVisibility(View.VISIBLE);
                                if (arg0.getId() == idCajaButton) {

                                    DCTButtonTask t = new DCTButtonTask();
                                    t.execute();

                                }

                                for (int i = 0; i < bs.size(); i++) {
                                    ls.get(i).setVisibility(View.GONE);
                                }
                            } else {
                                contlay.setVisibility(View.GONE);
                            }
                        }

                    });

                    int n = 0;
                    datos = b.getStringArrayList("SUBELEMENT");
                    if (datos != null) {
                        pares = new ArrayList<>();

                        for (int i = 0; i < datos.size(); i++) {
                            String[] d = datos.get(i).split(";");
                            String p = "";
                            n = d.length;
                            if (i == 0) {
                                for (int j = 0; j < n; j++) {
                                    if (j == 0) {
                                        p += d[j];
                                    } else
                                        p += ";" + d[j];
                                }

                            } else {
                                for (int j = 0; j < n; j++) {
                                    if (j == 0) {
                                        p += d[j];
                                    } else
                                        p += ";" + d[j];
                                }
                            }
                            pares.add(p);
                        }
                        //contlay.addView(dibujarTabla(1, datos.size(), n, "#FFFFFF"));
                    }


                    //boton.setCompoundDrawablesWithIntrinsicBounds(left,0, ic_buttom1, 0);

                    boton.setCompoundDrawablesWithIntrinsicBounds(left, null, getResources().getDrawable(ic_buttom1), null);
                    LContenido.addView(linearLayoutBoton);

                    contlay.setId(str2int(newIdCont));
                    LContenido.addView(contlay);

                    ids_contenidos.add(str2int(newIdCont));
                    ids_botones.add(boton.getId());

                }

                Log.d(TAG, "BOTONES DISPONIBLES: " + ids_botones.toString());
                Log.d(TAG, "Contenidos DISPONIBLES: " + ids_contenidos.toString());
            }
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }

    }

    private Boton getButton(ArrayList<String> botones, String buscado) {
        for (String b : botones) {
            String[] datos = b.split(";");
            Boton button = new Boton(datos[0], datos[1], datos[2]);
            if (button.getId().equals(buscado)) return button;
        }

        return null;
    }


    public void fatc(View v) {
        if (bloqueo.getState("Fact")) {
            Funciones.makeResultAlert(mContext, bloqueo.getMsg("Fact"), false).show();
        } else {
            Intent i = new Intent(this, FactActivity.class);
            i.putStringArrayListExtra("DECOS", decos_reg);
            i.putStringArrayListExtra("CLIENTE", datos_cliente_reg);
            i.putExtra("PHONE", Phone);
            startActivity(i);
            Log.d("TEST", decos_reg.toString());
            Log.d("TEST", datos_cliente_reg.toString());
        }
    }

    //TODO: str2int
    public static int str2int(String a) {
        char[] b = a.toCharArray();
        int r = 0;
        int i = 1;
        for (char c : b) {
            r += Character.getNumericValue(c) * i;
            i += 10;
        }
        return r;
    }

    //TODO: Dibujar tabla
    @SuppressWarnings("deprecation")
    public TableLayout dibujarTabla(int tamBorde, int numeroFilas, int numeroColumnas, String colorBorde) {
        TableLayout tabla = new TableLayout(this);
        if (numeroFilas > 0 && numeroColumnas > 0) {
            TableRow fila;

            ArrayList<String> cabeceras = new ArrayList<>();

            int anchoDefault = getWindowManager().getDefaultDisplay().getWidth() / 30;
            int ancho;
            int columna;

            TextView tmp = new TextView(this);
            RelativeLayout temp = new RelativeLayout(this);
            for (int i = 0; i < numeroFilas; i++) {
                columna = 0;
                final String[] cols = pares.get(i).split(";");
                fila = new TableRow(this);

                for (int j = 0; j < numeroColumnas - 1; j++) {
                    Boolean telefono_clickable = false;
                    RelativeLayout borde = new RelativeLayout(this);
                    borde.setPadding(tamBorde, tamBorde, 0, 0);

                    if (j == numeroColumnas - 1) { // SI ES LA ULTIMA COLUMNA
                        borde.setPadding(tamBorde, tamBorde, tamBorde, 0);
                    }
                    if (i == numeroFilas - 1) { //SI ES LA ULTIMA FILA
                        borde.setPadding(tamBorde, tamBorde, 0, tamBorde);
                        if (j == numeroColumnas - 1) { // Y ULTIMA COLUMNA
                            borde.setPadding(tamBorde, tamBorde, tamBorde, tamBorde);
                        }
                    }

                    TextView texto = new TextView(this);

                    final String tipo;
                    final String valor;


                    borde.setBackgroundColor(Color.parseColor(colorBorde));

                    texto.setGravity(Gravity.CLIP_HORIZONTAL);
                    texto.setPadding(2, 4, 2, 4);
                    texto.setTextColor(Color.BLACK);

                    /** SI ES CABECERA **/
                    if (i == 0) {
                        tipo = "";
                        String[] asd = cols[j].split("/");
                        if (asd.length > 1) {
                            valor = cols[j].split("/")[1]; //TOMAMOS EL VALOR DE LA CABECERA
                        } else
                            valor = "--";
                        /** SETTEAMOS LOS COLORES */
                        texto.setBackgroundColor(Color.BLUE);
                        texto.setTextColor(Color.WHITE);
                        borde.setBackgroundColor(Color.BLUE);


                        cabeceras.add(valor); /** REGISTRAMOS LA CABECERA */
                        texto.setText(valor); /**AGREGAMOS EL VALOR A LA TABLA*/
                        ancho = valor.length() * anchoDefault; // LE DEFINIMOS EL ANCHO
                        texto.setWidth(ancho);
                        //if(j != numeroColumnas-1)
                        borde.addView(texto);
                    } else { /** SI NO ES CABECERA... EL RESTO DEL ARREGLO*/


                        texto.setBackgroundColor(Color.WHITE); /** FONDO BLANCO*/
                        String[] asd = cols[columna].split("/");

                        tipo = asd[0];  /** TOMAMOS EL TIPO DE COLUMNA */
                        if (asd.length == 2)
                            valor = asd[1]; /** TOMAMOS EL VALOR */
                        else
                            valor = "--";


                        ancho = valor.length() * anchoDefault;
                        if (cabeceras.get(j).equals(tipo)) { // SI EL TIPO CORRESPONDE A LA COLUMNA
                            texto.setText(valor); //* LO AGREGO
                            columna++;
                            texto.setWidth(ancho);
                        } else {
                            texto.setText("");
                            texto.setWidth(anchoDefault);
                        }


                        if (cols[cols.length - 1].split("/")[1].equals("2")) {
                            texto.setTextColor(Color.BLUE);
                            parActual = cols[0].split("/")[1];
                        }

                        if (tipo.compareTo("TELEFONO") == 0 && valor.compareTo(" ") != 0 && valor.length() > 2) {
                            String[] estado = cols[2].split("/");
                            if (estado.length == 2 && estado[1].equals("OCUPADO BA")) {
                                telefono_clickable = true;
                                texto.setBackgroundResource(R.drawable.custom_button_search);
                            }


                        }
                        if (tipo.compareTo("ESTADO") == 0 && tipoProcedimiento != 0) {
                            texto.setBackgroundResource(R.drawable.custom_button_search);
                        }

                        borde.addView(texto);
                    }

                    if (j == 2 && tipo.compareTo("ESTADO") == 0 && tipoProcedimiento != 0) {
                        borde.setClickable(true);
                        borde.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(VistaTopologica.topo);
                                dialog.setTitle("Cambiando el par " + parActual + " por " + cols[0].split("/")[1] + ":");
                                dialog.setMessage("¿Desea continuar?");
                                dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActionButtonTask a;
                                        if (!parActual.equals(cols[0].split("/")[1])) {
                                            a = new ActionButtonTask(Area, Phone, parActual + ";" + cols[0].split("/")[1], "ACTION301");
                                            a.execute();
                                        } else
                                            Funciones.makeResultAlert(mContext, "Error\nPar actualmente asignado", false).show();

                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {


                                    }
                                });
                                dialog.show();
                            }
                        });
                    }

                    if (telefono_clickable) {
                        borde.setClickable(true);
                        borde.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent certificar = new Intent(VistaTopologica.topo, CertificarPar.class);
                                String[] areaphone = valor.split(" ");
                                certificar.putExtra("AREA", areaphone[0]);
                                certificar.putExtra("PHONE", areaphone[1]);
                                startActivity(certificar);

                            }
                        });
                    }
                    /** AGREGO LA COLUMNA A LA FILA **/
                    fila.addView(borde);

                }

                /** AGREGO LA FILA A LA TABLA */
                tabla.addView(fila);
            }
        }
        return tabla;
    }


    /**
     * Clase que ejecuta la consulta ActionButton al WebService y muestra los resultados.
     * Phone: Telefono asociado a la accion
     * Type: En este caso se envian los numeros de serie asociados a la accion
     * Action: Contiene el tipo de accion (ACTION101,ACTION102,ACTION103,ACTOIN201,ACTION301)
     */
    private class ActionButtonTask extends AsyncTask<String, Integer, ArrayList<String>> {

        private final ProgressDialog dialog = new ProgressDialog(VistaTopologica.this);
        private String Area;
        private String Phone;
        private String Type;
        private String Action;

        public ActionButtonTask(String area, String phone, String type, String action) {
            super();
            this.Area = area;
            this.Phone = phone;
            this.Type = type;
            this.Action = action;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> todo;
            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEI = telephonyManager.getDeviceId();
                String IMSI = telephonyManager.getSimSerialNumber();
                String response = SoapRequestMovistar.getActionButton(IMEI, IMSI, this.Area,this.Phone, this.Type, this.Action);
                todo = XMLParser.getReturnCode(response);

            } catch (Exception e1) {
                e1.printStackTrace();
                todo = null;
            }

            return todo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage("Enviando solicitud de accion...");
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            super.onPostExecute(s);
            if (s != null && s.size() > 1)
                Funciones.makeResultAlert(mContext, s.get(1), false).show();
            if (s == null)
                Funciones.makeResultAlert(mContext, "Error en la conexión", false).show();
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }
    }

    public LinearLayout putButtonAddTV(String name) {
        isTV = true;
        LinearLayout agregar = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layoutbuttonadd, null, false);
        Button add = (Button) agregar.findViewById(R.id.buttonAdd);
        add.setText(name);
        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dial = new Dialog(VistaTopologica.topo);
                dial.setContentView(R.layout.new_deco_view);
                final EditText serieDeco = (EditText) dial.findViewById(R.id.editText);
                final EditText serieTarjeta = (EditText) dial.findViewById(R.id.editText2);
                ImageButton ok = (ImageButton) dial.findViewById(R.id.bOK);
                ImageButton nok = (ImageButton) dial.findViewById(R.id.bNOK);
                dial.setTitle("Ingrese datos nuevo DECO:");
                ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (serieDeco.getText().toString().length() == 10
                                && serieTarjeta.getText().toString().length() == 10) {

                            tipo = serieDeco.getText().toString()
                                    + ";" + serieTarjeta.getText().toString();
                            Log.w(TAG, "ACTION401--TYPE=" + tipo);
                            ActionButtonTask ab = new ActionButtonTask(Area, Phone, tipo, "ACTION401");
                            ab.execute();
                            dial.dismiss();
                        } else {
                            Funciones.makeResultAlert(mContext, "Debe ingresar ambos números de serie de 10 dígitos", false).show();
                        }
                    }
                });
                nok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dial.dismiss();
                    }
                });

                dial.show();
            }
        });
        if (tipoProcedimiento == 0) {
            add.setVisibility(View.GONE);
        }
        return agregar;
    }

    public LinearLayout putButtonReactivateTV(String name) {
        isTV = true;
        LinearLayout agregar = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layoutbuttonreactivate, null, false);
        Button add = (Button) agregar.findViewById(R.id.buttonAdd);
        add.setText(name);
        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(VistaTopologica.topo);
                b.setMessage("¿Desea reactivar DECO?");
                b.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActionButtonTask ab = new ActionButtonTask(Area, Phone, "", "ACTION102");
                        ab.execute();
                        dialog.dismiss();
                    }
                });
                b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                b.show();
            }
        });
        if (tipoProcedimiento == 0) {
            add.setVisibility(View.GONE);
        }
        return agregar;
    }

    private class DCTButtonTask extends AsyncTask<String, String, String> {
        ProgressDialog dialog;

        boolean stateOK = false;

        private DCTButtonTask() {
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
            dialog.setMessage("Buscando información...\nEste proceso podría tardar unos segundos.");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String query;
                if (Phone.equals("2")) {
                    query = URLs.DCTRESOURCE;
                } else {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String IMEI = telephonyManager.getDeviceId();
                    String IMSI = telephonyManager.getSimSerialNumber();
                    query = SoapRequestMovistar.getDCTOnDemand(IMEI, IMSI, Area,Phone);
                }

                ArrayList<String> retorno = XMLParser.getReturnCode(query);
                int code = Integer.valueOf(retorno.get(0));

                if (code == 0) {
                    stateOK = true;
                    return query;
                } else
                    return retorno.get(1);

            } catch (ConnectTimeoutException e) {
                e.printStackTrace();
                return "Se agoto el tiempo de espera. Por favor reintente";
            } catch (Exception e) {
                Log.e("DCTACTIONBUTTON", e.getMessage());
                return "Error";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            if (stateOK) {
                try {
                    ArrayList<Bundle> dct = XMLParser.getResourcesNew(s);
                    Log.d("DCTACTIONBUTTON", "LARGO " + dct.size());
                    dibujar_dct(dct.get(0), XMLParser.getStateButtonsDCT(s));

                } catch (Exception e) {
                    e.printStackTrace();
                    Funciones.makeResultAlert(mContext, "Error en la información obtenida. Por favor reintente.", false).show();
                }
            } else {
                Funciones.makeResultAlert(mContext, s, false).show();
            }

            if (dialog.isShowing()) dialog.dismiss();
        }
    }

    private void dibujar_dct(Bundle b, boolean buttonDCT) {
        View contView = LContenido.findViewById(idCajaContent);
        ((LinearLayout) contView).removeAllViews();
        View v = LayoutInflater.from(mContext).inflate(R.layout.dct_view, null, false);
        LinearLayout contlay = (LinearLayout) v.findViewById(R.id.dct_content);
        Button dct = (Button) v.findViewById(R.id.dct_button);
        dct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent para = new Intent(mContext, DCT.class);
                para.putExtra("PHONE", Phone);
                para.putExtra("AREA", Area);
                startActivity(para);
            }
        });

        if (buttonDCT) {
            dct.setVisibility(View.VISIBLE);
        } else
            dct.setVisibility(View.GONE);

        ArrayList<String> datos = b.getStringArrayList("IDENTIFICATION");

        if (datos != null) {
            for (String d : datos) {
                final String[] lineas = d.split(";");
                for (int k = 0; k < lineas.length; k++) {
                    String[] informacion = lineas[k].split("&");
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(linea, null, false);

                    TextView izq = (TextView) linearLayout.findViewById(R.id.textView1);
                    izq.setTextColor(getResources().getColor(R.color.black));


                    TextView der = (TextView) linearLayout.findViewById(R.id.textView2);
                    der.setTextColor(getResources().getColor(R.color.celeste));

                    izq.setText(" " + informacion[0] + ":  ");
                    if (informacion.length == 1)
                        der.setText("---");
                    else
                        der.setText(informacion[1]);

                    contlay.addView(linearLayout);
                }
            }
        }

        int n = 0;
        datos = b.getStringArrayList("SUBELEMENT");
        if (datos != null) {
            pares = new ArrayList<>();

            for (int i = 0; i < datos.size(); i++) {
                String[] d = datos.get(i).split(";");
                String p = "";
                n = d.length;
                if (i == 0) {
                    for (int j = 0; j < n; j++) {
                        if (j == 0) {
                            p += d[j];
                        } else
                            p += ";" + d[j];
                    }

                } else {
                    for (int j = 0; j < n; j++) {
                        if (j == 0) {
                            p += d[j];
                        } else
                            p += ";" + d[j];
                    }
                }
                pares.add(p);
            }
            contlay.addView(dibujarTabla(1, datos.size(), n, "#FFFFFF"));
        }
        ((LinearLayout) contView).addView(v);

    }
}
