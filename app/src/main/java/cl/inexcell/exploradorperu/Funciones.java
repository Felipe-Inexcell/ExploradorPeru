package cl.inexcell.exploradorperu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Funciones {
    private static int TYPE_OK = 0;
    private static int TYPE_NOK = 1;

    private static final String PATTERN_EMAIL = "" +
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PATTERN_RUT = "([0-9]{7,8})-([kK0-9]{1})";


    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }


    public static boolean validateEmail(String email) {

        // Compiles the given regular expression into a pattern.
        Pattern pattern = Pattern.compile(PATTERN_EMAIL);

        // Match the given input against this pattern
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();

    }

    public static boolean validateRut(String rut) {

        // Compiles the given regular expression into a pattern.
        Pattern pattern = Pattern.compile(PATTERN_RUT);

        // Match the given input against this pattern
        Matcher matcher = pattern.matcher(rut);
        return matcher.matches();

    }

    public static Toast makeToast(Context context, String message){
        Toast t = new Toast(context);
        TextView text = new TextView(context);
        LinearLayout l = new LinearLayout(context);
        text.setText(message);
        text.setTextColor(context.getResources().getColor(R.color.celeste));
        text.setTextSize(24);
        l.setBackgroundResource(R.drawable.fondo1);
        l.setPadding(32,32,32,32);
        l.addView(text);
        t.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                0, 0);
        t.setView((View)l);
        return t;
    }

    public static AlertDialog makeDialog(Context context, String message){
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_message, null, false);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(v)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }

    public static TextView makeTextView(Context ctx, String text){
        TextView t = new TextView(ctx);
        t.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        t.setText(text);
        return t;
    }
    public static TextView makeTextView(Context ctx, String text,int tipo){
        TextView t = new TextView(ctx);
        if(tipo == 0)
        t.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if(tipo == 1)
        t.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        t.setGravity(Gravity.CENTER_HORIZONTAL);
        t.setText(text);
        return t;
    }

    public static AlertDialog.Builder makeAlert(Context CONTEXTO, String TITULO, String MENSAJE, Boolean CANCELABLE){
        AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXTO);
        if(TITULO != null)
            builder.setTitle(TITULO);
        if(MENSAJE != null)
            builder.setMessage(MENSAJE);
        builder.setCancelable(CANCELABLE);
        return builder;

    }

    public static AlertDialog.Builder makeExitAlert(Context CONTEXTO, final ArrayList<Activity> Actividades){
        AlertDialog.Builder b = new AlertDialog.Builder(CONTEXTO);
        b.setMessage("¿Seguro que desea cerrar la aplicación?");
        b.setPositiveButton("Sí, Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(Activity a: Actividades)
                    if(a!= null)a.finish();
                dialog.dismiss();
            }
        });
        b.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return b;

    }

    public static AlertDialog.Builder makeBackAlert(final Context CONTEXTO){
        AlertDialog.Builder b = new AlertDialog.Builder(CONTEXTO);
        b.setMessage("¿Seguro que desea volver?");
        b.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity)CONTEXTO).finish();

            }
        });
        b.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return b;

    }

    public static AlertDialog.Builder makeResultAlert(Context CONTEXTO, String MENSAJE,Boolean CANCELABLE){
        AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXTO);
        builder.setMessage(MENSAJE);
        builder.setCancelable(CANCELABLE);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder;

    }

    public static File Update(String apkurl){
        try {
            URL url = new URL(apkurl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            String[] spliit = apkurl.split("/");

                    String PATH = Environment.getExternalStorageDirectory() + "/Explorador/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, spliit[spliit.length-1]);
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();//till here, it works fine - .apk is download to my sdcard in download file


            return outputFile;
        } catch (IOException e) {
            return null;
        }
    }



}
