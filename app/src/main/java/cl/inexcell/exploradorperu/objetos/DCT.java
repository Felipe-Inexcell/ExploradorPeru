package cl.inexcell.exploradorperu.objetos;

import java.util.ArrayList;

/**
 * Created by felip on 18/08/2015.
 */
public class DCT {
    ArrayList<DCTParamCaja> parametrosCajas;
    ArrayList<DCTParamFono> parametrosFonos;


    public DCT() {
    }

    public ArrayList<DCTParamCaja> getParametrosCajas() {
        return parametrosCajas;
    }

    public void setParametrosCajas(ArrayList<DCTParamCaja> parametrosCajas) {
        this.parametrosCajas = parametrosCajas;
    }

    public ArrayList<DCTParamFono> getParametrosFonos() {
        return parametrosFonos;
    }

    public void setParametrosFonos(ArrayList<DCTParamFono> parametrosFonos) {
        this.parametrosFonos = parametrosFonos;
    }
}
