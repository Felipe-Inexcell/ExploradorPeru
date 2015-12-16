package cl.inexcell.exploradorperu.objetos;

import java.util.ArrayList;

/**
 * Created by felip on 18/08/2015.
 */
public class DCTParamFono {
    String par;
    String area;
    String fono;
    String tipo;
    String perfil;
    ArrayList<DCTParamFonoCabecera> parametrosFonosCabecera;

    public DCTParamFono(String par, String area, String fono, String tipo, String perfil) {
        this.par = par;
        this.area = area;
        this.fono = fono;
        this.tipo = tipo;
        this.perfil = perfil;
    }

    public String getPar() {
        return par;
    }

    public void setPar(String par) {
        this.par = par;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getFono() {
        return fono;
    }

    public void setFono(String fono) {
        this.fono = fono;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public ArrayList<DCTParamFonoCabecera> getParametrosFonosCabecera() {
        return parametrosFonosCabecera;
    }

    public void setParametrosFonosCabecera(ArrayList<DCTParamFonoCabecera> parametrosFonosCabecera) {
        this.parametrosFonosCabecera = parametrosFonosCabecera;
    }
}
