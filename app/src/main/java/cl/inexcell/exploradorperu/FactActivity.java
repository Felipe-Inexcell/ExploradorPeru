package cl.inexcell.exploradorperu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cl.inexcell.exploradorperu.objetos.Deco;
import cl.inexcell.exploradorperu.objetos.ElementFormulario;
import cl.inexcell.exploradorperu.objetos.Formulario;
import cl.inexcell.exploradorperu.objetos.FormularioEnvio;
import cl.inexcell.exploradorperu.objetos.ParametrosEnvioForm;
import cl.inexcell.exploradorperu.objetos.ParametrosFormulario;
import cl.inexcell.exploradorperu.preferences.FactSave;


public class FactActivity extends Activity implements View.OnClickListener {
    public static Activity p;
    Context mContext;
    LinearLayout fatcLayout;
    TextView nombreTecnico;
    Formulario formularioFinal;
    String Phone;
    Boolean isEmail = true;
    FactSave reg;

    int positionInsert = 0;

    ArrayList<String> decosUsados;

    ArrayList<FormularioEnvio> formularioEnvio;

    ArrayList<Integer> ids_botones, ids_contenidos, ids_campos;
    ArrayList<EditText> editTextsBA, editTextsTelef, editTextsTelev, editTextsSeriesDeco, editTextsSeriesTarjeta, editTextsSeries, editTextsCierre;
    ArrayList<TextView> TextsBA, TextsTelef, TextsTelev, TextsDeco, TextsSeries, TextsCierre;
    ArrayList<String> editTextsRetiro, TextsRetiro;

    AlertDialog.Builder dialog_preview;
    View preview_view;
    ImageView IVpreview;
    Button verCarnet, verFirma;

    private Bitmap b = null;
    private Bitmap firma = null;
    private static int TAKE_PICTURE = 1;
    private static int SELECT_PICTURE = 2;
    String name = Environment.getExternalStorageDirectory() + "/carnet.jpg"; //picture filename
    View subContentCPY = null;
    View tabHeaderCPY = null;


    private void saveData() {
        for (int i = 0; i < editTextsBA.size(); i++) {
            EditText tmp = editTextsBA.get(i);
            reg.setValue(TextsBA.get(i).getText() + "" + tmp.getId(), tmp.getText().toString());
        }
        for (int i = 0; i < editTextsTelef.size(); i++) {
            EditText tmp = editTextsTelef.get(i);
            reg.setValue(TextsTelef.get(i).getText() + "" + tmp.getId(), tmp.getText().toString());
        }
        for (int i = 0; i < editTextsTelev.size(); i++) {
            EditText tmp = editTextsTelev.get(i);
            reg.setValue(TextsTelev.get(i).getText() + "" + tmp.getId(), tmp.getText().toString());
        }
        for (int i = 0; i < editTextsCierre.size(); i++) {
            EditText tmp = editTextsCierre.get(i);
            reg.setValue(TextsCierre.get(i).getText() + "" + tmp.getId(), tmp.getText().toString());
        }

        for (int i = 0; i < editTextsRetiro.size(); i++) {
            reg.setValue("FACTRETIRO" + i, TextsRetiro.get(i) + ";" + editTextsRetiro.get(i));
        }

        if(decosUsados != null){
            reg.setValue("NDECOSUSADOS", decosUsados.size());
            for(int i = 0; i<decosUsados.size(); i++){
                reg.setValue("DECOUSADO" + i, decosUsados.get(i));
            }
        }
        /*if (b != null) {
            reg.setValue("FACTFOTO", Funciones.encodeTobase64(b));
        }
        if (firma != null)
            reg.setValue("FACTFIRMA", Funciones.encodeTobase64(firma));*/
    }

    /*private void loadImages() {
        String sign = reg.getValue("FACTFIRMA");
        String photo = reg.getValue("FACTFOTO");
        try {
            if (!photo.equals("0"))
                b = Funciones.decodeBase64(photo);
            if (!sign.equals("0"))
                firma = Funciones.decodeBase64(sign);

            if (b != null) verCarnet.setEnabled(true);
            if (firma != null) verFirma.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        reg.clear();
    }*/

    private void loadRetiros() {
        int i = 0;
        String registro;
        while (!(registro = reg.getValue("FACTRETIRO" + i)).equals("0")) {
            String[] info = registro.split(";");
            final int lastInster = TextsRetiro.size();
            if (info[0].equals("Decos")) {

                final View uno = LayoutInflater.from(mContext).inflate(R.layout.tabrow_retiro_deco, null, false);

                ImageButton borrar = (ImageButton) uno.findViewById(R.id.button_erase);
                borrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ViewManager) subContentCPY).removeView(uno);
                        Log.d("remove", "" + lastInster);
                        editTextsRetiro.remove(lastInster);
                        TextsRetiro.remove(lastInster);
                    }
                });


                TextView elemento = (TextView) uno.findViewById(R.id.elemento);
                EditText serieDeco = (EditText) uno.findViewById(R.id.edit_deco);
                EditText serieTarjeta = (EditText) uno.findViewById(R.id.edit_tarjeta);
                elemento.setText(info[0]);
                serieDeco.setText(info[1]);
                serieTarjeta.setText(info[2]);
                ((LinearLayout) subContentCPY).addView(uno);


                editTextsRetiro.add(info[1] + ";" + info[2]);
                TextsRetiro.add(info[0]);
                Log.d("positionInsert", "" + positionInsert);
                Log.d("TextRetiro.size()", TextsRetiro.size() + "");
                positionInsert++;
                tabHeaderCPY.setVisibility(View.VISIBLE);
            } else {
                final View uno = LayoutInflater.from(mContext).inflate(R.layout.tabrow_retiro, null, false);
                TextView elemento = (TextView) uno.findViewById(R.id.elemento);

                ImageButton borrar = (ImageButton) uno.findViewById(R.id.button_erase);
                borrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ViewManager) subContentCPY).removeView(uno);
                        Log.d("remove", "" + lastInster);
                        editTextsRetiro.remove(lastInster);
                        TextsRetiro.remove(lastInster);
                    }
                });

                EditText serieVista = (EditText) uno.findViewById(R.id.edit_retiro);
                elemento.setText(info[0]);
                serieVista.setText(info[1]);
                ((LinearLayout) subContentCPY).addView(uno);


                editTextsRetiro.add(info[1]);
                TextsRetiro.add(info[0]);

                Log.d("TextRetiro.size()", TextsRetiro.size() + "");
                positionInsert++;
                tabHeaderCPY.setVisibility(View.VISIBLE);
            }
            i++;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fatc);

        /** ASIGNACIONES */
        mContext = this;
        p = this;

        reg = new FactSave(this);

        Phone = getIntent().getExtras().getString("PHONE");

        fatcLayout = (LinearLayout) findViewById(R.id.contenido_fatc);
        nombreTecnico = (TextView) findViewById(R.id.nombreTecnico);

        editTextsBA = new ArrayList<>();
        editTextsTelef = new ArrayList<>();
        editTextsTelev = new ArrayList<>();
        editTextsSeries = new ArrayList<>();
        editTextsSeriesDeco = new ArrayList<>();
        editTextsSeriesTarjeta = new ArrayList<>();
        editTextsCierre = new ArrayList<>();
        TextsBA = new ArrayList<>();
        TextsTelef = new ArrayList<>();
        TextsTelev = new ArrayList<>();
        TextsSeries = new ArrayList<>();
        TextsDeco = new ArrayList<>();
        TextsCierre = new ArrayList<>();
        editTextsRetiro = new ArrayList<>();
        TextsRetiro = new ArrayList<>();


        Obtener_Formulario task = new Obtener_Formulario(this);
        task.execute();
    }

    private List<String> getElementos(ArrayList<ParametrosFormulario> parametros) {
        List<String> result = new ArrayList<>();
        for (ParametrosFormulario p : parametros) {
            result.add(p.getAtributo());
        }
        return result;
    }

    private int getInputType(String type) {
        int input;
        switch (type) {
            case "int":
                input = InputType.TYPE_CLASS_NUMBER;
                break;
            case "numeric":
                input = InputType.TYPE_CLASS_NUMBER;
                break;
            case "decimal":
                input = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
                break;
            case "text":
                input = InputType.TYPE_CLASS_TEXT;
                break;
            default:
                input = InputType.TYPE_CLASS_TEXT;
                break;
        }

        return input;

    }

    private void addDeco(String clave){
        if(decosUsados == null)
            decosUsados = new ArrayList<>();

        decosUsados.add(clave);
    }

    private boolean generarVista(Formulario f) {
        formularioFinal = f;
        ids_botones = new ArrayList<>();
        ids_contenidos = new ArrayList<>();
        ids_campos = new ArrayList<>();

        nombreTecnico.setText(f.getNombreTecnico());

        for (final ElementFormulario ef : f.getElements()) {
            View buttonLayout = LayoutInflater.from(mContext).inflate(R.layout.layoutbuttontopologica, null, false);
            Button boton = (Button) buttonLayout.findViewById(R.id.button1);
            boton.setCompoundDrawablesWithIntrinsicBounds(ef.getDrawable(), 0, R.drawable.ic_bottom1, 0);
            boton.setText(ef.getValue());
            boton.setId(str2int("boton" + ef.getType() + ef.getValue()));


            final View subContenido = LayoutInflater.from(mContext).inflate(R.layout.contenidolayout, null, false);
            subContenido.setVisibility(View.GONE);
            subContenido.setId(str2int("contenido" + ef.getType() + ef.getValue()));

            if (ef.getType().compareTo("Broadband") == 0 || ef.getType().compareTo("Telephony") == 0 || ef.getType().compareTo("DigitalTelevision") == 0) {
                View tableHeader1 = LayoutInflater.from(mContext).inflate(R.layout.tabheader, null, false);
                ((LinearLayout) subContenido).addView(tableHeader1);
            } else if (ef.getType().compareTo("remove") == 0) {
                LinearLayout agregar = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layoutbuttonadd, null, false);
                Button add = (Button) agregar.findViewById(R.id.buttonAdd);
                add.setText("Ingresar Retiro");
                final View tableHeader2 = LayoutInflater.from(mContext).inflate(R.layout.tabheader_retiro, null, false);
                tabHeaderCPY = tableHeader2;
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final View content = LayoutInflater.from(mContext).inflate(R.layout.view_ingresar_retiro, null, false);
                        final Spinner spinner = (Spinner) content.findViewById(R.id.retiro_elementos);
                        final LinearLayout decos = (LinearLayout) content.findViewById(R.id.layout_deco);
                        final LinearLayout general = (LinearLayout) content.findViewById(R.id.layout_general);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, getElementos(ef.getParameters()));
                        spinner.setAdapter(adapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                if (parent.getSelectedItem().toString().compareTo("Decos") == 0) {
                                    decos.setVisibility(View.VISIBLE);
                                    general.setVisibility(View.GONE);

                                } else {
                                    general.setVisibility(View.VISIBLE);
                                    decos.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });


                        final AlertDialog d = new AlertDialog.Builder(mContext)
                                .setView(content)
                                .setTitle("Datos del Retiro")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        d.show();
                        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final int lastInster = TextsRetiro.size();
                                if (spinner.getSelectedItem().toString().compareTo("Decos") == 0) {

                                    final View uno = LayoutInflater.from(mContext).inflate(R.layout.tabrow_retiro_deco, null, false);

                                    EditText serie1 = (EditText) content.findViewById(R.id.editText13);
                                    EditText serie2 = (EditText) content.findViewById(R.id.editText14);

                                    ImageButton borrar = (ImageButton) uno.findViewById(R.id.button_erase);
                                    borrar.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ((ViewManager) subContenido).removeView(uno);
                                            Log.d("remove", "" + lastInster);
                                            editTextsRetiro.remove(lastInster);
                                            TextsRetiro.remove(lastInster);
                                        }
                                    });


                                    TextView elemento = (TextView) uno.findViewById(R.id.elemento);
                                    if (serie1.getText().toString().compareTo("") != 0 && serie2.getText().toString().compareTo("") != 0 && serie1.getText().toString().length() == 10 && serie2.getText().toString().length() == 10) {
                                        EditText serieDeco = (EditText) uno.findViewById(R.id.edit_deco);
                                        EditText serieTarjeta = (EditText) uno.findViewById(R.id.edit_tarjeta);
                                        elemento.setText(spinner.getSelectedItem().toString());
                                        serieDeco.setText(serie1.getText());
                                        serieTarjeta.setText(serie2.getText());
                                        ((LinearLayout) subContenido).addView(uno);


                                        editTextsRetiro.add(serie1.getText() + ";" + serie2.getText());
                                        TextsRetiro.add(spinner.getSelectedItem().toString());
                                        Log.d("positionInsert", "" + positionInsert);
                                        Log.d("TextRetiro.size()", TextsRetiro.size() + "");
                                        positionInsert++;
                                        tableHeader2.setVisibility(View.VISIBLE);
                                        d.dismiss();
                                    } else {
                                        Funciones.makeResultAlert(mContext, "Debe ingresar ambos números de serie de 10 digitos", false).show();
                                    }
                                } else {
                                    final View uno = LayoutInflater.from(mContext).inflate(R.layout.tabrow_retiro, null, false);
                                    EditText serie = (EditText) content.findViewById(R.id.editText15);
                                    TextView elemento = (TextView) uno.findViewById(R.id.elemento);

                                    ImageButton borrar = (ImageButton) uno.findViewById(R.id.button_erase);
                                    borrar.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ((ViewManager) subContenido).removeView(uno);
                                            Log.d("remove", "" + lastInster);
                                            editTextsRetiro.remove(lastInster);
                                            TextsRetiro.remove(lastInster);
                                        }
                                    });

                                    if (serie.getText().toString().compareTo("") != 0) {
                                        EditText serieVista = (EditText) uno.findViewById(R.id.edit_retiro);
                                        elemento.setText(spinner.getSelectedItem().toString());
                                        serieVista.setText(serie.getText());
                                        ((LinearLayout) subContenido).addView(uno);


                                        editTextsRetiro.add(serie.getText().toString());
                                        TextsRetiro.add(spinner.getSelectedItem().toString());

                                        Log.d("TextRetiro.size()", TextsRetiro.size() + "");
                                        positionInsert++;
                                        tableHeader2.setVisibility(View.VISIBLE);
                                        d.dismiss();
                                    } else {
                                        Funciones.makeResultAlert(mContext, "Debe ingresar el numero de serie", false).show();
                                    }
                                }
                                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                            }
                        });
                    }
                });
                ((LinearLayout) subContenido).addView(agregar);


                tableHeader2.setVisibility(View.GONE);
                ((LinearLayout) subContenido).addView(tableHeader2);
            }


            for (ParametrosFormulario pf : ef.getParameters()) {
                switch (ef.getType()) {
                    case "Broadband":
                        View tableRow1 = LayoutInflater.from(mContext).inflate(R.layout.tabrow, null, false);
                        TextView material1 = (TextView) tableRow1.findViewById(R.id.materialName);
                        EditText cantidad1 = (EditText) tableRow1.findViewById(R.id.cantField);
                        cantidad1.setInputType(getInputType(pf.getTypeDataInput()));
                        editTextsBA.add(cantidad1);
                        TextsBA.add(material1);


                        material1.setText(pf.getAtributo());
                        cantidad1.setHint(pf.getValue());
                        cantidad1.setEnabled(pf.getEnabled());

                        String valor = reg.getValue(pf.getAtributo() + "" + cantidad1.getId());
                        if (!valor.equals("0"))
                            cantidad1.setText(valor);

                        ((LinearLayout) subContenido).addView(tableRow1);
                        break;
                    case "Telephony":
                        View tableRow2 = LayoutInflater.from(mContext).inflate(R.layout.tabrow, null, false);
                        TextView material2 = (TextView) tableRow2.findViewById(R.id.materialName);
                        EditText cantidad2 = (EditText) tableRow2.findViewById(R.id.cantField);
                        cantidad2.setInputType(getInputType(pf.getTypeDataInput()));
                        editTextsTelef.add(cantidad2);
                        TextsTelef.add(material2);

                        material2.setText(pf.getAtributo());
                        cantidad2.setHint(pf.getValue());
                        cantidad2.setEnabled(pf.getEnabled());

                        valor = reg.getValue(pf.getAtributo() + "" + cantidad2.getId());
                        if (!valor.equals("0"))
                            cantidad2.setText(valor);
                        ((LinearLayout) subContenido).addView(tableRow2);
                        break;
                    case "DigitalTelevision":
                        if (pf.getAtributo().compareTo("DecosSerie") == 0) {
                            for (Deco deco : pf.getDecos()) {
                                String[] cabeceras = {"DECO:", "SERIE DECO:", "SERIE TARJETA:"};
                                String[] datos = {deco.getLabel(), deco.getSerieDeco(), deco.getSerieTarjeta()};
                                for (int i = 0; i < 3; i++) {
                                    View vista = LayoutInflater.from(this).inflate(R.layout.layouttextotexto, null, false);
                                    ((TextView) vista.findViewById(R.id.textView1)).setText(cabeceras[i]);
                                    ((TextView) vista.findViewById(R.id.textView2)).setText(datos[i]);
                                    if (i == 0) {
                                        vista.setBackgroundResource(R.color.celeste);
                                    }
                                    ((LinearLayout) subContenido).addView(vista);
                                }
                            }
                        } else if (pf.getAtributo().equals("Decos")) {
                            View tableRow3 = LayoutInflater.from(mContext).inflate(R.layout.tabrowadd, null, false);
                            TextView material3 = (TextView) tableRow3.findViewById(R.id.materialName);
                            final EditText cantidad3 = (EditText) tableRow3.findViewById(R.id.cantField);
                            final ImageButton add = (ImageButton) tableRow3.findViewById(R.id.btn_add);
                            cantidad3.setEnabled(false);
                            cantidad3.setInputType(InputType.TYPE_NULL);

                            final LinearLayout decosContent = new LinearLayout(mContext);
                            decosContent.setOrientation(LinearLayout.VERTICAL);
                            decosContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            final TextView cabecera = new TextView(mContext);
                            cabecera.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            cabecera.setTextColor(Color.BLUE);
                            cabecera.setVisibility(View.GONE);
                            cabecera.setText("DECOS USADOS");
                            cabecera.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            cabecera.setGravity(Gravity.CENTER_HORIZONTAL);
                            cabecera.setPadding(0,(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),0,(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));




                            editTextsTelev.add(cantidad3);
                            TextsTelev.add(material3);

                            material3.setText(pf.getAtributo());



                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    View addDeco = LayoutInflater.from(mContext).inflate(R.layout.view_ingresar_deco, null, false);
                                    final EditText serieDeco = (EditText) addDeco.findViewById(R.id.editText1);
                                    final EditText serieTarj = (EditText) addDeco.findViewById(R.id.editText2);


                                    AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                                    b.setTitle("Ingrese datos del Deco");
                                    b.setView(addDeco);
                                    b.setPositiveButton("Aceptar", null);
                                    b.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    final AlertDialog d = b.create();
                                    d.show();

                                    d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final View rowDeco = LayoutInflater.from(mContext).inflate(R.layout.tabrow_decos, null, false);
                                            final EditText Deco = (EditText) rowDeco.findViewById(R.id.edit_deco);
                                            final EditText Tarj = (EditText) rowDeco.findViewById(R.id.edit_tarjeta);
                                            final ImageButton delete = (ImageButton) rowDeco.findViewById(R.id.button_erase);

                                            final String clave = serieDeco.getText().toString()+";"+serieTarj.getText().toString();
                                            delete.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    decosContent.removeView(rowDeco);
                                                    decosUsados.remove(clave);
                                                    cantidad3.setText(""+(Integer.parseInt(cantidad3.getText().toString())-1));
                                                    if(Integer.parseInt(cantidad3.getText().toString()) == 0){
                                                        cabecera.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                            if(serieDeco.getText().toString().length() == 10 && serieTarj.getText().toString().length() == 10){

                                                cantidad3.setText(String.valueOf(Integer.parseInt(cantidad3.getText().toString()) + 1));
                                                Deco.setText(serieDeco.getText().toString());
                                                Tarj.setText(serieTarj.getText().toString());
                                                addDeco(clave);
                                                decosContent.addView(rowDeco);
                                                cabecera.setVisibility(View.VISIBLE);
                                                d.dismiss();
                                            }else{
                                                AlertDialog.Builder error = new AlertDialog.Builder(mContext);
                                                error.setTitle("Error!");
                                                error.setMessage("Debe ingresar ambos numeros de serie de 10 digitos");
                                                error.setPositiveButton("CERRAR", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                error.show();
                                            }


                                        }
                                    });




                                }
                            });
                            decosContent.addView(tableRow3);
                            decosContent.addView(cabecera);

                            int v = reg.getIntValue("NDECOSUSADOS");
                            if (v > 0) {
                                cantidad3.setText(""+v);
                                cabecera.setVisibility(View.VISIBLE);
                                for(int x = 0; x < v; x++){
                                    final String key = reg.getValue("DECOUSADO"+x);
                                    addDeco(key);

                                    String[] spleet = key.split(";");

                                    final View rowDeco = LayoutInflater.from(mContext).inflate(R.layout.tabrow_decos, null, false);
                                    final EditText Deco = (EditText) rowDeco.findViewById(R.id.edit_deco);
                                    final EditText Tarj = (EditText) rowDeco.findViewById(R.id.edit_tarjeta);
                                    final ImageButton delete = (ImageButton) rowDeco.findViewById(R.id.button_erase);

                                    Deco.setText(spleet[0]);
                                    Tarj.setText(spleet[1]);


                                    delete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            decosContent.removeView(rowDeco);
                                            decosUsados.remove(key);
                                            cantidad3.setText(""+(Integer.parseInt(cantidad3.getText().toString())-1));
                                        }
                                    });
                                    decosContent.addView(rowDeco);
                                }

                            }else
                                cantidad3.setText("0");

                            ((LinearLayout) subContenido).addView(decosContent);
                        }else {
                            View tableRow3 = LayoutInflater.from(mContext).inflate(R.layout.tabrow, null, false);
                            TextView material3 = (TextView) tableRow3.findViewById(R.id.materialName);
                            EditText cantidad3 = (EditText) tableRow3.findViewById(R.id.cantField);
                            cantidad3.setInputType(getInputType(pf.getTypeDataInput()));
                            editTextsTelev.add(cantidad3);
                            TextsTelev.add(material3);

                            material3.setText(pf.getAtributo());
                            cantidad3.setHint(pf.getValue());
                            cantidad3.setEnabled(pf.getEnabled());
                            valor = reg.getValue(pf.getAtributo() + "" + cantidad3.getId());
                            if (!valor.equals("0"))
                                cantidad3.setText(valor);
                            ((LinearLayout) subContenido).addView(tableRow3);
                        }
                        break;
                    case "remove":
                        subContentCPY = subContenido;
                        break;
                    case "ClosingData":
                        View linea;
                        switch (pf.getAtributo()) {
                            case "Rut":
                                linea = LayoutInflater.from(mContext).inflate(R.layout.view_texto_campo, null, false);
                                TextView title1 = (TextView) linea.findViewById(R.id.title);
                                EditText campo1 = (EditText) linea.findViewById(R.id.campo);

                                editTextsCierre.add(campo1);
                                TextsCierre.add(title1);

                                if (pf.getValue().compareTo("0") != 0)
                                    campo1.setHint(pf.getValue());
                                title1.setText(pf.getAtributo());
                                campo1.setId(str2int("cierrecampo" + pf.getAtributo() + pf.getTypeInput() + pf.getTypeDataInput()));
                                campo1.setInputType(InputType.TYPE_CLASS_TEXT);

                                ids_campos.add(campo1.getId());
                                valor = reg.getValue(title1.getText() + "" + campo1.getId());
                                if (!valor.equals("0")) {
                                    campo1.setText(valor);
                                }

                                ((LinearLayout) subContenido).addView(linea);
                                break;
                            case "Email":
                                linea = LayoutInflater.from(mContext).inflate(R.layout.view_texto_campo_rut, null, false);
                                CheckBox title2 = (CheckBox) linea.findViewById(R.id.isEmail);
                                final EditText campo2 = (EditText) linea.findViewById(R.id.campo);
                                editTextsCierre.add(campo2);
                                if (pf.getValue().compareTo("0") != 0)
                                    campo2.setHint(pf.getValue());
                                title2.setText(pf.getAtributo());
                                campo2.setId(str2int("cierrecampo" + pf.getAtributo() + pf.getTypeInput() + pf.getTypeDataInput()));
                                campo2.setInputType(InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                                title2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        isEmail = isChecked;
                                        if (isChecked) {
                                            campo2.setVisibility(View.VISIBLE);
                                        } else
                                            campo2.setVisibility(View.GONE);
                                    }
                                });

                                ids_campos.add(campo2.getId());
                                TextView title3 = new TextView(mContext);
                                title3.setText("Email");
                                TextsCierre.add(title3);

                                valor = reg.getValue(title2.getText() + "" + campo2.getId());
                                if (!valor.equals("0")) {
                                    campo2.setText(valor);
                                }

                                ((LinearLayout) subContenido).addView(linea);
                                break;
                            case "FotoCarnet":
                                View botoneras = LayoutInflater.from(this).inflate(R.layout.view_firmafoto, null, false);
                                Button firmar = (Button) botoneras.findViewById(R.id.boton_firma);
                                verFirma = (Button) botoneras.findViewById(R.id.button_verfirma);
                                verCarnet = (Button) botoneras.findViewById(R.id.button_vercarnet);

                                firmar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        View lay = LayoutInflater.from(mContext).inflate(R.layout.view_signature, null, false);
                                        final SignaturePad signature = (SignaturePad) lay.findViewById(R.id.signature_pad);

                                        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                                        b.setView(lay);
                                        b.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                firma = signature.getSignatureBitmap();
                                                firma = Bitmap.createScaledBitmap(firma, 640, 480, true);
                                                verFirma.setEnabled(true);
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        b.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        b.setNeutralButton("Borrar", null);
                                        final AlertDialog dialog = b.create();
                                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                                            @Override
                                            public void onShow(DialogInterface d) {

                                                Button b = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                                b.setOnClickListener(new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View view) {
                                                        signature.clear();
                                                    }
                                                });
                                            }
                                        });

                                        dialog.show();
                                    }
                                });

                                verFirma.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog_preview = new AlertDialog.Builder(mContext);
                                        dialog_preview.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        preview_view = LayoutInflater.from(mContext).inflate(R.layout.view_preview, null, false);
                                        IVpreview = (ImageView) preview_view.findViewById(R.id.preview);
                                        IVpreview.setImageBitmap(firma);
                                        dialog_preview.setView(preview_view);
                                        dialog_preview.setTitle("Vista Previa Firma");
                                        dialog_preview.show();
                                    }
                                });

                                verCarnet.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog_preview = new AlertDialog.Builder(mContext);
                                        dialog_preview.setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        preview_view = LayoutInflater.from(mContext).inflate(R.layout.view_preview, null, false);
                                        IVpreview = (ImageView) preview_view.findViewById(R.id.preview);
                                        IVpreview.setImageBitmap(b);
                                        dialog_preview.setView(preview_view);
                                        dialog_preview.setTitle("Vista Previa Carnet");
                                        dialog_preview.show();
                                    }
                                });

                                ((LinearLayout) subContenido).addView(botoneras);
                                break;
                            default:
                                if (pf.getTypeInput().compareTo("MultilineBox") == 0) {

                                    linea = LayoutInflater.from(mContext).inflate(R.layout.view_texto_campo_multilinea, null, false);

                                } else {
                                    linea = LayoutInflater.from(mContext).inflate(R.layout.view_texto_campo, null, false);
                                }
                                TextView title4 = (TextView) linea.findViewById(R.id.title);
                                EditText campo4 = (EditText) linea.findViewById(R.id.campo);

                                editTextsCierre.add(campo4);
                                TextsCierre.add(title4);


                                if (pf.getValue().compareTo("0") != 0)
                                    campo4.setHint(pf.getValue());
                                title4.setText(pf.getAtributo());
                                campo4.setId(str2int("cierrecampo" + pf.getAtributo() + pf.getTypeInput() + pf.getTypeDataInput()));
                                ids_campos.add(campo4.getId());

                                valor = reg.getValue(title4.getText() + "" + campo4.getId());
                                if (!valor.equals("0")) {
                                    campo4.setText(valor);
                                }
                                ((LinearLayout) subContenido).addView(linea);
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }


            boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    ArrayList<Button> bs = new ArrayList<>();
                    ArrayList<View> ls = new ArrayList<>();

                    int posicion = ids_botones.indexOf(arg0.getId());
                    for (int i = 0; i < ids_botones.size(); i++) {
                        if (i != posicion) {
                            Button b = (Button) fatcLayout.findViewById(ids_botones.get(i));
                            View v = fatcLayout.findViewById(ids_contenidos.get(i));
                            bs.add(b);
                            ls.add(v);
                        }
                    }

                    if (subContenido.getVisibility() == View.GONE) {
                        subContenido.setVisibility(View.VISIBLE);
                        for (int i = 0; i < bs.size(); i++) {
                            ls.get(i).setVisibility(View.GONE);
                        }
                    } else {
                        subContenido.setVisibility(View.GONE);
                    }
                }
            });

            fatcLayout.addView(buttonLayout);
            fatcLayout.addView(subContenido);

            ids_contenidos.add(subContenido.getId());
            ids_botones.add(boton.getId());

        }

        loadRetiros();
        //loadImages();
        fatcLayout.addView(putCasaCerradaBTN());
        return true;
    }

    private View putCasaCerradaBTN() {
        Button v = new Button(this);
        v.setText("Casa Cerrada");

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.setMargins(0, 10, 0, 10);
        param.gravity = Gravity.RIGHT;
        v.setLayoutParams(param);
        v.setPadding(16, 0, 16, 0);
        v.setBackgroundResource(R.drawable.custom_button_blue);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCasaCerrada();
            }
        });
        v.setTextColor(Color.WHITE);

        return v;
    }

    private void openCasaCerrada() {
        Intent i = new Intent(this, CasaCerrada.class);
        i.putExtra("PHONE", Phone);
        startActivity(i);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            default:
                break;
        }
    }

    public void shutdown(View v) {
        ArrayList<Activity> actividades = new ArrayList<>();
        actividades.add(Principal.p);
        actividades.add(VistaTopologica.topo);
        actividades.add(this);
        Funciones.makeExitAlert(this, actividades).show();
        saveData();
    }

    /**
     * TODO * Boton Guardar Informacion *
     */
    public void guardarInformacion(View view) {
        /*Intent n = new Intent(this, ActividadPDF.class);
        startActivity(n);
        */

        formularioEnvio = new ArrayList<>();
        FormularioEnvio form;
        ArrayList<ParametrosEnvioForm> parametrosEnvioForms;

        Boolean isOK = true;
        for (ElementFormulario element : formularioFinal.getElements()) {
            if (!isOK)
                break;
            parametrosEnvioForms = new ArrayList<>();
            form = new FormularioEnvio();
            ArrayList<ParametrosFormulario> parametros = element.getParameters();
            for (int i = 0; i < parametros.size(); i++) {
                if (!isOK)
                    break;
                ParametrosEnvioForm p = new ParametrosEnvioForm();
                switch (element.getType()) {
                    case "Broadband":
                        if (editTextsBA.get(i).getText().toString().compareTo("") != 0) {
                            p.setAttribute(TextsBA.get(i).getText().toString());
                            p.setValue(editTextsBA.get(i).getText().toString());
                            parametrosEnvioForms.add(p);
                        } else {
                            if (parametros.get(i).getRequired()) {
                                Funciones.makeResultAlert(mContext, "El parametro " + TextsBA.get(i).getText() + " es obligatorio", false).show();
                                isOK = false;
                            } else {
                                p.setAttribute(TextsBA.get(i).getText().toString());
                                p.setValue("0");
                                parametrosEnvioForms.add(p);
                            }
                        }
                        break;
                    case "Telephony":
                        if (editTextsTelef.get(i).getText().toString().compareTo("") != 0) {
                            p.setAttribute(TextsTelef.get(i).getText().toString());
                            p.setValue(editTextsTelef.get(i).getText().toString());
                            parametrosEnvioForms.add(p);
                        } else {
                            if (parametros.get(i).getRequired()) {
                                Funciones.makeResultAlert(mContext, "El parametro " + TextsTelef.get(i).getText() + " es obligatorio", false).show();
                                isOK = false;
                            } else {
                                p.setAttribute(TextsTelef.get(i).getText().toString());
                                p.setValue("0");
                                parametrosEnvioForms.add(p);
                            }
                        }
                        break;
                    case "DigitalTelevision":
                        if (parametros.get(i).getAtributo().compareTo("DecosSerie") == 0)
                            break;
                        if (editTextsTelev.get(i).getText().toString().compareTo("") != 0) {
                            p.setAttribute(TextsTelev.get(i).getText().toString());
                            p.setValue(editTextsTelev.get(i).getText().toString());
                            parametrosEnvioForms.add(p);
                        } else {
                            if (parametros.get(i).getRequired()) {
                                Funciones.makeResultAlert(mContext, "El parametro " + TextsTelev.get(i).getText() + " es obligatorio", false).show();
                                isOK = false;
                            } else {
                                p.setAttribute(TextsTelev.get(i).getText().toString());
                                p.setValue("0");
                                parametrosEnvioForms.add(p);
                            }
                        }
                        break;
                    case "ClosingData":
                        if (parametros.get(i).getAtributo().compareTo("FotoCarnet") == 0) {
                            if (b != null && firma != null) {
                                p.setAttribute("Firma");
                                p.setValue(Funciones.encodeTobase64(firma));
                                parametrosEnvioForms.add(p);

                                p = new ParametrosEnvioForm();
                                p.setAttribute("Carnet");
                                p.setValue(Funciones.encodeTobase64(b));
                                parametrosEnvioForms.add(p);
                            } else {
                                Funciones.makeResultAlert(mContext, "Debe registrar la Firma y una fotografía de la Cédula de Identidad", false).show();
                                isOK = false;
                            }
                        } else if (editTextsCierre.get(i).getText().toString().compareTo("") != 0) {

                            if (parametros.get(i).getAtributo().compareTo("Email") == 0) {
                                if (isEmail && Funciones.validateEmail(editTextsCierre.get(i).getText().toString())) {
                                    p.setAttribute(TextsCierre.get(i).getText().toString());
                                    p.setValue(editTextsCierre.get(i).getText().toString());
                                    parametrosEnvioForms.add(p);
                                } else {
                                    Funciones.makeResultAlert(mContext, "Email incorrecto", false).show();
                                    isOK = false;
                                }
                            } else if (parametros.get(i).getAtributo().compareTo("Rut") == 0) {
                                if (Funciones.validateRut(editTextsCierre.get(i).getText().toString())) {
                                    p.setAttribute(TextsCierre.get(i).getText().toString());
                                    p.setValue(editTextsCierre.get(i).getText().toString());
                                    parametrosEnvioForms.add(p);
                                } else {
                                    Funciones.makeResultAlert(mContext, "Rut incorrecto\nFormato: 12345678-1", false).show();
                                    isOK = false;
                                }
                            } else {
                                p.setAttribute(TextsCierre.get(i).getText().toString());
                                p.setValue(editTextsCierre.get(i).getText().toString());
                                parametrosEnvioForms.add(p);
                            }
                        } else {
                            if (parametros.get(i).getRequired()) {
                                if (parametros.get(i).getAtributo().compareTo("Email") != 0) {
                                    Funciones.makeResultAlert(mContext, "El parametro " + TextsCierre.get(i).getText() + " es obligatorio", false).show();
                                    isOK = false;
                                } else {
                                    if (isEmail) {
                                        Funciones.makeResultAlert(mContext, "El parametro " + TextsCierre.get(i).getText() + " es obligatorio", false).show();
                                        isOK = false;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            if (element.getType().compareTo("remove") == 0) {
                parametrosEnvioForms = new ArrayList<>();
                ParametrosEnvioForm p;
                for (int k = 0; k < TextsRetiro.size(); k++) {
                    Log.d("PRUEBA", TextsRetiro.get(k) + " - " + editTextsRetiro.get(k));
                    p = new ParametrosEnvioForm();
                    p.setAttribute(TextsRetiro.get(k));
                    p.setValue(editTextsRetiro.get(k));
                    parametrosEnvioForms.add(p);
                }
                form.setType(element.getType());
                form.setParametros(parametrosEnvioForms);
                formularioEnvio.add(form);
            } else {
                form.setType(element.getType());
                form.setParametros(parametrosEnvioForms);
                formularioEnvio.add(form);
            }
        }

        for (FormularioEnvio f : formularioEnvio) {
            Log.d("FORM", f.getType());
            for (ParametrosEnvioForm p : f.getParametros()) {
                Log.d("FORM", "**" + p.getAttribute() + ":" + p.getValue());
            }
        }

        if (isOK) {
            Enviar u = new Enviar(mContext);
            u.execute();
        }
    }

    /**
     * Boton Camara *
     */
    public void capturarImagen(View v) {
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
                    b = data.getParcelableExtra("data");
                }
            } else {
                b = BitmapFactory.decodeFile(name);

            }
        }
        try {
            b = Bitmap.createScaledBitmap(b, 640, 480, true);
            verCarnet.setEnabled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    /**
     * Boton Volver *
     */

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        volver(null);
    }

    public void volver(View view) {

        InputMethodManager imm = (InputMethodManager) p.getSystemService(Context.INPUT_METHOD_SERVICE);
        View foco = p.getCurrentFocus();
        if (foco == null || !imm.hideSoftInputFromWindow(foco.getWindowToken(), 0)) {
            saveData();
            Funciones.makeBackAlert(this).show();

        }
    }

    /*
    Todo: Obtener Formulario
     */
    class Obtener_Formulario extends AsyncTask<String, String, Formulario> {
        Context tContext;
        ProgressDialog dialog;

        public Obtener_Formulario(Context context) {
            tContext = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(tContext);
            dialog.setMessage("Cargando Formulario...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Formulario doInBackground(String... params) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEI = telephonyManager.getDeviceId();
                String IMSI = telephonyManager.getSimSerialNumber();
                String request;
                if (Phone.compareTo("2") != 0)
                    request = SoapRequestMovistar.postCertifyDSL(Phone, IMEI, IMSI, "?", "?");
                else
                    request = getResponseNew();
                Formulario parse = XMLParser.getForm(request);
                return parse;

            } catch (Exception e) {
                Log.e("FactActivity", e.getMessage() + ":" + e.getCause());
                return null;
            }

        }

        @Override
        protected void onPostExecute(Formulario formulario) {
            if (formulario != null) {

                if (formulario.getReturnCode() == 0)
                    if (generarVista(formulario) && dialog.isShowing()) {
                        dialog.dismiss();
                    } else {
                        Funciones.makeAlert(p, null, formulario.getReturnDescription(), false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Activity) mContext).finish();
                                    }
                                }).show();
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }

            } else {
                Funciones.makeAlert(p, "Error en la conexión", "Compruebe su conexión a internet y reintente.", false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) mContext).finish();
                            }
                        }).show();
                if (dialog.isShowing())
                    dialog.dismiss();
            }

        }

        private String getResponseNew() {
            return "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:tns=\"urn:Demo\">" +
                    "<SOAP-ENV:Body>" +
                    "<ns1:PostCertifyDSLResponse xmlns:ns1=\"urn:Demo\">" +
                    "<ResponsePostCertifyDSL xsi:type=\"tns:ResponsePostCertifyDSL\">" +
                    "<Operation xsi:type=\"tns:OperationType1\">" +
                    "<OperationCode xsi:type=\"xsd:string\">?</OperationCode>" +
                    "<OperationId xsi:type=\"xsd:string\">?</OperationId>" +
                    "<DateTime xsi:type=\"xsd:string\">?</DateTime>" +
                    "<IdUser xsi:type=\"xsd:string\">?</IdUser>" +
                    "<IMEI xsi:type=\"xsd:string\">?</IMEI>" +
                    "<IMSI xsi:type=\"xsd:string\">?</IMSI>" +
                    "<Telefono xsi:type=\"xsd:string\">2</Telefono>" +
                    "<Television xsi:type=\"xsd:string\">1</Television>" +
                    "<BandaAncha xsi:type=\"xsd:string\">1</BandaAncha>" +
                    "<NombreTecnico xsi:type=\"xsd:string\">CARRASCO ZURITA LUIS</NombreTecnico>" +
                    "</Operation>" +
                    "<Service xsi:type=\"tns:ServicePostCertifyDSLOut\">" +
                    "<PostCertifyDSL xsi:type=\"tns:PostCertifyDSLOut\">" +
                    "<Output xsi:type=\"tns:PostCertifyDSLOutData\">" +
                    "<Element xsi:type=\"tns:ElementType3\">" +
                    "<Id xsi:type=\"xsd:string\">0</Id>" +
                    "<Type xsi:type=\"xsd:string\">Broadband</Type>" +
                    "<Value xsi:type=\"xsd:string\">SERVICIO BANCHA ANCHA</Value>" +
                    "<Identification xsi:type=\"tns:IdentificationType3\">" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Int2p</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">int</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">JumperROBl</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">decimal</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">SpliterADSL</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">text</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "</Identification>" +
                    "</Element>" +
                    "<Element xsi:type=\"tns:ElementType3\">" +
                    "<Id xsi:type=\"xsd:string\">1</Id>" +
                    "<Type xsi:type=\"xsd:string\">DigitalTelevision</Type>" +
                    "<Value xsi:type=\"xsd:string\">SERVICIO TELEVISION</Value>" +
                    "<Identification xsi:type=\"tns:IdentificationType3\">" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Antena</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">TarjetaTV</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Conectores</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">LnbOP</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">RG6</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Decos</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">DecosSerie</Attribute>" +
                    "<Value xsi:type=\"xsd:string\"/>" +
                    "<typeInput xsi:type=\"xsd:string\">label</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">text</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "<SeriesDecos xsi:type=\"tns:SeriesDecosType\">" +
                    "<Label xsi:type=\"xsd:string\">Easy Digital / ED-S8</Label>" +
                    "<SerieDeco xsi:type=\"xsd:string\">1859024150</SerieDeco>" +
                    "<SerieTarjeta xsi:type=\"xsd:string\">0324530640</SerieTarjeta>" +
                    "</SeriesDecos>" +
                    "</Parameters>" +
                    "</Identification>" +
                    "</Element>" +
                    "<Element xsi:type=\"tns:ElementType3\">" +
                    "<Id xsi:type=\"xsd:string\">2</Id>" +
                    "<Type xsi:type=\"xsd:string\">Telephony</Type>" +
                    "<Value xsi:type=\"xsd:string\">SERVICIO TELEFONIA</Value>" +
                    "<Identification xsi:type=\"tns:IdentificationType3\">" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Acometida</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Int1P</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">JumperAZAM</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "</Identification>" +
                    "</Element>" +
                    "<Element xsi:type=\"tns:ElementType3\">" +
                    "<Id xsi:type=\"xsd:string\">3</Id>" +
                    "<Type xsi:type=\"xsd:string\">remove</Type>" +
                    "<Value xsi:type=\"xsd:string\">RETIROS</Value>" +
                    "<Identification xsi:type=\"tns:IdentificationType3\">" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Phone</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Modem</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Decos</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">others</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">numeric</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">false</Required>" +
                    "</Parameters>" +
                    "</Identification>" +
                    "</Element>" +
                    "<Element xsi:type=\"tns:ElementType3\">" +
                    "<Id xsi:type=\"xsd:string\">4</Id>" +
                    "<Type xsi:type=\"xsd:string\">ClosingData</Type>" +
                    "<Value xsi:type=\"xsd:string\">DATOS DE CIERRE</Value>" +
                    "<Identification xsi:type=\"tns:IdentificationType3\">" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Nombre</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">text</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">true</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Rut</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">text</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">true</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">Email</Attribute>" +
                    "<Value xsi:type=\"xsd:string\">0</Value>" +
                    "<typeInput xsi:type=\"xsd:string\">Box</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">text</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">true</Required>" +
                    "</Parameters>" +
                    "<Parameters xsi:type=\"tns:ParametersType3\">" +
                    "<Attribute xsi:type=\"xsd:string\">FotoCarnet</Attribute>" +
                    "<Value xsi:type=\"xsd:string\"/>" +
                    "<typeInput xsi:type=\"xsd:string\">button</typeInput>" +
                    "<typeDataInput xsi:type=\"xsd:string\">text</typeDataInput>" +
                    "<Enabled xsi:type=\"xsd:string\">true</Enabled>" +
                    "<Required xsi:type=\"xsd:string\">true</Required>" +
                    "</Parameters>" +
                    "</Identification>" +
                    "</Element>" +
                    "<Return xsi:type=\"tns:ReturnType\">" +
                    "<Code xsi:type=\"xsd:string\">0</Code>" +
                    "<Description xsi:type=\"xsd:string\">Informacion enviada</Description>" +
                    "</Return>" +
                    "</Output>" +
                    "</PostCertifyDSL>" +
                    "</Service>" +
                    "</ResponsePostCertifyDSL>" +
                    "</ns1:PostCertifyDSLResponse>" +
                    "</SOAP-ENV:Body>" +
                    "</SOAP-ENV:Envelope>";
        }
    }

    /*
    TODO Enviar
     */
    class Enviar extends AsyncTask<String, String, ArrayList<String>> {
        Context eContext;
        ProgressDialog dialog;

        Enviar(Context eContext) {
            this.eContext = eContext;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(eContext);
            dialog.setMessage("Enviando Formulario...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEI = telephonyManager.getDeviceId();
                String IMSI = telephonyManager.getSimSerialNumber();
                String request = SoapRequestMovistar.guardarFact(Phone, IMEI, IMSI, "?", "?", formularioEnvio, decosUsados);
                //String request = getGuardarFact();
                ArrayList<String> parse = XMLParser.getReturnCodeForm(request);
                ArrayList<String> parseFoto;
                if (parse.get(0).compareTo("0") == 0) {
                    FormularioEnvio fotos = formularioEnvio.get(formularioEnvio.size() - 1);
                    for (ParametrosEnvioForm p : fotos.getParametros()) {
                        if (p.getAttribute().compareTo("Carnet") == 0) {
                            request = SoapRequestMovistar.subirFoto(parse.get(2), p.getValue(), 0, IMEI, IMSI);
                            parseFoto = XMLParser.getReturnCode(request);
                            if (parseFoto.get(0).compareTo("0") == 0) {
                                Log.d("ENVIANDO", "CARNET OK");
                            } else {
                                Log.e("ENVIANDO", "CARNET NOK");
                            }
                            break;
                        }
                        if (p.getAttribute().compareTo("Firma") == 0) {
                            request = SoapRequestMovistar.subirFoto(parse.get(2), p.getValue(), 1, IMEI, IMSI);
                            parseFoto = XMLParser.getReturnCode(request);
                            if (parseFoto.get(0).compareTo("0") == 0) {
                                Log.d("ENVIANDO", "FIRMA OK");
                            } else {
                                Log.e("ENVIANDO", "FIRMA NOK");
                            }
                        }
                        break;
                    }

                }
                return parse;
            } catch (Exception e) {
                Log.e("ENVIANDO", e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause());
                return null;
            }

        }

        @Override
        protected void onPostExecute(ArrayList<String> response) {
            AlertDialog.Builder builder;
            if (response != null) {
                if (response.get(0).compareTo("0") == 0) {
                    if (response.size() >= 2) {
                        //Toast.makeText(p, response.get(1), Toast.LENGTH_LONG).show();
                        builder = Funciones.makeAlert(p, null, response.get(1), false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (VistaTopologica.topo != null)
                                            VistaTopologica.topo.finish();
                                        ((Activity) mContext).finish();
                                        reg.clearPreferences();
                                    }
                                });
                    } else {
                        //Toast.makeText(mContext, "Formulado enviado", Toast.LENGTH_LONG).show();
                        builder = Funciones.makeAlert(p, null, "Formulado enviado", false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (VistaTopologica.topo != null)
                                            VistaTopologica.topo.finish();
                                        ((Activity) mContext).finish();
                                        reg.clearPreferences();
                                    }
                                });
                    }
                } else {
                    if (response.size() >= 2) {
                        //Toast.makeText(mContext, response.get(1), Toast.LENGTH_LONG).show();
                        builder = Funciones.makeAlert(p, null, response.get(1), false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                    } else
                        //Toast.makeText(mContext,"Error al enviar. Por favor reintente.",Toast.LENGTH_LONG).show();
                        builder = Funciones.makeAlert(p, null, "Error al enviar. Por favor reintente.", false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                }
            } else {
                //Toast.makeText(mContext,"Error desconocido, no se pudo enviar.",Toast.LENGTH_LONG).show();
                builder = Funciones.makeAlert(p, null, "Error desconocido, no se pudo enviar.", false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
                if (!dialog.isShowing()) builder.show();
            }
        }
    }


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


    private String getGuardarFact() {
        return "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:tns=\"urn:Demo\">" +
                "<SOAP-ENV:Body>" +
                "<ns1:GuardarFactResponse xmlns:ns1=\"urn:Demo\">" +
                "<ResponseGuardarFact xsi:type=\"tns:ResponseGuardarFact\">" +
                "<Operation xsi:type=\"tns:OperationType2\">" +
                "<OperationCode xsi:type=\"xsd:string\">?</OperationCode>" +
                "<OperationId xsi:type=\"xsd:string\">?</OperationId>" +
                "<DateTime xsi:type=\"xsd:string\">?</DateTime>" +
                "<IdUser xsi:type=\"xsd:string\">?</IdUser>" +
                "<IMEI xsi:nil=\"true\" xsi:type=\"xsd:string\">?</IMEI>" +
                "<IMSI xsi:type=\"xsd:string\">?</IMSI>" +
                "<IdFact xsi:type=\"xsd:string\">2015070100006</IdFact>" +
                "</Operation>" +
                "<Return xsi:type=\"tns:GuardarFactParameterType\">" +
                "<Code xsi:type=\"xsd:string\">0</Code>" +
                "<Description xsi:type=\"xsd:string\">EXITOSO</Description>" +
                "</Return>" +
                "</ResponseGuardarFact>" +
                "</ns1:GuardarFactResponse>" +
                "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>";
    }

}
