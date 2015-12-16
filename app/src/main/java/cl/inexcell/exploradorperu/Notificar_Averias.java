package cl.inexcell.exploradorperu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.conn.ConnectTimeoutException;

import cl.inexcell.exploradorperu.daemon.MyLocationListener;

public class Notificar_Averias extends Activity {

	private ArrayList<String> res;
    Context mContext;
    Activity p;

	private Bitmap foto;
	private Spinner s, dano, clas, afec;
	private EditText et;
	
	private String TAG = "Localizar Avería";

    Geocoder geocoder;
	
	private Bitmap b = null;
	private static int TAKE_PICTURE = 1;
	private static int SELECT_PICTURE = 2;
	private String name = "";
	private MyLocationListener gps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// Activity sin parte superior
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_notificar_averias);

        p= this;
        mContext = this;
        geocoder = new Geocoder(this);
        gps = new MyLocationListener(getApplicationContext());

        s = (Spinner) findViewById(R.id.Spinner021);  //Element
		dano = (Spinner) findViewById(R.id.Spinner011); //TypeDamage
        clas = (Spinner) findViewById(R.id.Spinner013); //Clasification
        afec = (Spinner) findViewById(R.id.Spinner012); //Afectation
		et = (EditText) findViewById(R.id.editText11);  //Observation
		name = Environment.getExternalStorageDirectory() + "/test.jpg"; //picture filename
		
		clas.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
                SearchElement searchElement = new SearchElement(mContext,arg2,arg0.getSelectedItem().toString());
                searchElement.execute();

			}
			
		});

        SearchElement create = new SearchElement(this);
        create.execute();
	}

	public void shutdown(View v){
        ArrayList<Activity> actividades = new ArrayList<>();
        actividades.add(Principal.p);
        actividades.add(this);
        Funciones.makeExitAlert(this, actividades).show();
	}
	/** Boton Guardar Informacion **/
	public void guardarInformacion (View view){

        //Comprobamos que se haya ingresado una observación
		if(et.getText().toString().compareTo("") == 0){
            Funciones.makeResultAlert(mContext, "Debe ingresar una observación", false).show();
			return;
		}
        //Comprobamos que se haya tomado una fotografía
		if(b == null){
            Funciones.makeResultAlert(mContext, "Debe tomar una fotografía", false).show();
			return;
		}

        /* Tomamos los datos seleccionados de los combobox o spinner */
		final String observacion = et.getText().toString();
		final String objeto = s.getSelectedItem().toString();
		final String tipodano = dano.getSelectedItem().toString();
        final String afectacion = afec.getSelectedItem().toString();
        final String clasificacion = clas.getSelectedItem().toString();
		foto = b;

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_averia);
        dialog.setTitle("¿Todo Correcto?");


        // set the custom dialog components - text, image and button
        final TextView elemento = (TextView) dialog.findViewById(R.id.dialogaveria_elemento);
        TextView siniestro = (TextView) dialog.findViewById(R.id.dialogaveria_siniestro);
        TextView comentario = (TextView) dialog.findViewById(R.id.dialogaveria_comentario);
        TextView afecta = (TextView) dialog.findViewById(R.id.dialogaveria_afectacion);
        TextView clasifi = (TextView) dialog.findViewById(R.id.dialogaveria_clasificacion);

        elemento.setText(objeto);
        siniestro.setText(tipodano);
        comentario.setText(observacion);
        afecta.setText(afectacion);
        clasifi.setText(clasificacion);
        ImageView image = (ImageView) dialog.findViewById(R.id.dialogaveria_captura);
        image.setImageBitmap(b);

        ImageButton dialogOk = (ImageButton) dialog.findViewById(R.id.dialogaveria_ok);
        ImageButton dialogNok = (ImageButton) dialog.findViewById(R.id.dialogaveria_nok);
        // if button is clicked, close the custom dialog
        dialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(objeto != null && tipodano != null && afectacion != null && clasificacion != null){
                    Enviar_Averia ea = new Enviar_Averia(objeto, tipodano, afectacion, clasificacion, observacion, foto);
                    ea.execute();
                }else
                    Funciones.makeResultAlert(mContext, "Falta información", false).show();
            }
        });

        dialogNok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
	}
	
	/** Boton Camara **/
	public void capturarImagen(View view){
        Intent intent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        int item = TAKE_PICTURE;
        if (item==TAKE_PICTURE) {
            Uri output = Uri.fromFile(new File(name));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        } else if (item==SELECT_PICTURE){
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            item = SELECT_PICTURE;
        }
        startActivityForResult(intent, item);

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
	    } else if (requestCode == SELECT_PICTURE){
	    	Uri selectedImage = data.getData();
	    	InputStream is;
	    	try {
	    	    is = getContentResolver().openInputStream(selectedImage);
	    	    BufferedInputStream bis = new BufferedInputStream(is);
	    	    b = BitmapFactory.decodeStream(bis);
	    	    
	    	} catch (FileNotFoundException e) {}
	    }
	    try{
	    b = Bitmap.createScaledBitmap(b, 640, 480, true);
	    }catch(Exception ex){}
	    
	    
	}

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        volver(null);
    }
	
	/** Boton Volver **/
	public void volver(View view) {
        InputMethodManager imm = (InputMethodManager) p.getSystemService(Context.INPUT_METHOD_SERVICE);
        View foco = p.getCurrentFocus();
        if (foco == null || !imm.hideSoftInputFromWindow(foco.getWindowToken(), 0)) {
            Funciones.makeBackAlert(this).show();
        }
    }
	
private class Enviar_Averia extends AsyncTask<String,Integer,String> {
   		
   		private final ProgressDialog dialog = new ProgressDialog(Notificar_Averias.this);
        private String element, damage, affectation, classification, observation, address;
        Bitmap photo;

    public Enviar_Averia(String element, String damage, String affectation, String classification, String observation, Bitmap photo) {

        List<Address> matches;
        try {
            matches = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1);


        }catch (Exception e){matches = null;}

        if(matches != null){
            this.address = matches.get(0).toString();
        }
        else
            this.address ="desconocida";
        this.element = element;
        this.damage = damage;
        this.affectation = affectation;
        this.classification = classification;
        this.observation = observation;
        this.photo = photo;
    }


    protected void onPreExecute() {
 			this.dialog.setMessage("Enviando Avería Localizada...");
 			this.dialog.setCanceledOnTouchOutside(false);
 			this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
                    Funciones.makeAlert(mContext,null, "Operación Interrumpida", false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((Activity) mContext).finish();
                                }
                            }).show();
				}
			});
 		    this.dialog.show();
             //super.onPreExecute();
         }

    protected String doInBackground(String... params) {
   	    	
 			String respuesta = null;
   			
   			try 
   			{
   				TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
   				String IMEI = telephonyManager.getDeviceId();
   				String IMSI =  telephonyManager.getSimSerialNumber();

                Log.w("ENVIANDOAVERIA","element: "+this.element);
                Log.w("ENVIANDOAVERIA","damage: "+this.damage);
                Log.w("ENVIANDOAVERIA","class: "+this.classification);
                Log.w("ENVIANDOAVERIA","affect: "+this.affectation);
                Log.w("ENVIANDOAVERIA","address: "+this.address);
                Log.w("ENVIANDOAVERIA","obs: "+this.observation);

   				respuesta = SoapRequestMovistar.setLocation(Funciones.encodeTobase64(this.photo),
   																this.element,
   																this.damage,
                                                                this.classification,
                                                                this.affectation,
                                                                this.address,
                                                                this.observation,
   																Double.toString(gps.getLatitude()),
   																Double.toString(gps.getLongitude()),
   																IMEI,
   																IMSI);
   				
   			} catch (Exception e1) {
   				e1.printStackTrace();
   			}

   	        return respuesta;
   	    }
   	    

 		protected void onPostExecute(String result) {
 			
 			if (this.dialog.isShowing()) {
 		        this.dialog.dismiss();
 		     }
   			
   	    	if (result != null)
   	    	{
   	    		if(result.equals("GPS")){
                    Funciones.makeResultAlert(mContext, "Error con GPS", false).show();
   					return;
   	    		}
   	    			
   	    		try {

   	    			res = XMLParser.setLocation(result);

   	    			et.setText("");
   	    			b = foto = null;
                    Funciones.makeAlert(mContext, null, res.get(1), false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((Activity)mContext).finish();
                                }
                            }).show();
   	    	    	
   	    	    	// Vibrar al hacer click        
   	    	        Vibrator vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
   	    	        vibrator.vibrate(50);
   	    			
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
   	    	}
   	    	else
   	    	{
                Funciones.makeResultAlert(mContext, "Error en la conexión del servicio.\nRevise su conexión a Internet", false).show();
   	    	}
   	    }
   	}

private class SearchElement extends AsyncTask<String,Integer,String> {
		Context context;
		private ProgressDialog dialog;
        private String positionSelected;
        private String message;
        private String operationId;
    String errorMSG = "";

        public SearchElement(Context c,int position, String elementSelected){
            this.context = c;
            this.positionSelected = String.valueOf(position);
            this.message = "Buscando elementos de "+ elementSelected+"...";
            this.operationId = "02";
        }

        public SearchElement(Context c){
            this.context = c;
            this.positionSelected = "";
            this.message = "Cargando...";
            this.operationId = "01";

        }
		
		protected void onPreExecute() {
            this.dialog = new ProgressDialog(Notificar_Averias.this);
			this.dialog.setMessage(this.message);
			this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setCancelable(false);
		    this.dialog.show();
     }
		 
	    protected String doInBackground(String... params) {
	    	
			String response;
			
			try 
			{
				TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
				String IMEI = telephonyManager.getDeviceId();
				String IMSI =  telephonyManager.getSimSerialNumber();

                Log.d(TAG, "operationId: "+this.operationId+ " - posicion: "+this.positionSelected);
				response = SoapRequestMovistar.getDamage(IMEI,IMSI,this.operationId,this.positionSelected);
			} catch (ConnectTimeoutException timeoutException){
                Log.d("AVERIA", timeoutException.getMessage());
                errorMSG = "Se agotó el tiempo de espera con el servidor, verifique su conexión a internet";
                return null;
            }catch (Exception e1) {
                errorMSG = "Ha ocurrido un error, por favor reintente";
                Log.e(TAG, e1.getMessage());
                response = null;
			}


	        return response;
	    }
	    

		protected void onPostExecute(String result) {
			
			if (this.dialog.isShowing()) {
		        this.dialog.dismiss();
		     }
	    	if (result != null && this.operationId.equals("01"))
	    	{
                Log.d(TAG,"is 01");
	    		try {

	    			ArrayList<ArrayList<String>> responseParse =  XMLParser.getDamage(result);
                    Log.w(TAG, responseParse.toString());
                    List<String> classification = null, affectation = null, damage = null;
	    			for(ArrayList<String> response: responseParse){
                        if(response.get(0).equals("CLASSIFICATION"))
                            classification = response.subList(1, response.size());
                        if(response.get(0).equals("AFFECTATION"))
                            affectation = response.subList(1, response.size());
                        if(response.get(0).equals("TYPE"))
                            damage = response.subList(1, response.size());
                    }

                    if(classification == null || affectation == null || damage == null){
                        Funciones.makeAlert(mContext, null, "Ha ocurrido un error. Por favor reintente", false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Activity)mContext).finish();
                                    }
                                }).show();

                    }

					ArrayAdapter<String> adapterClas = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, classification);
                    adapterClas.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    clas.setAdapter(adapterClas);
                    adapterClas.notifyDataSetChanged();

					ArrayAdapter<String> adapterAffec = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, affectation);
                    adapterAffec.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    afec.setAdapter(adapterAffec);
                    adapterAffec.notifyDataSetChanged();

					ArrayAdapter<String> adapterDamage = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, damage);
					adapterDamage.setDropDownViewResource(R.layout.spinner_dropdown_item);
					dano.setAdapter(adapterDamage);
					adapterDamage.notifyDataSetChanged();

	    			
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    	if (result != null && this.operationId.equals("02"))
	    	{
                Log.d(TAG,"is 02");
	    		try {

	    			ArrayList<ArrayList<String>> responseParse =  XMLParser.getDamage(result);
                    List<String> element = null;
	    			for(ArrayList<String> response: responseParse){
                        if(response.get(0).equals("ELEMENT"))
                            element = response.subList(1, response.size());
                    }

                    if(element == null){
                        Funciones.makeAlert(mContext, null, "Operacion Interrumpida", false)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Activity)mContext).finish();
                                    }
                                }).show();

                    }

					ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, element);
					adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
					s.setAdapter(adapter);
					adapter.notifyDataSetChanged();


				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    	if(result == null)
	    	{
                Funciones.makeAlert(mContext, null, errorMSG, false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)mContext).finish();
                            }
                        }).show();
	    	}
	    }
	}


}
