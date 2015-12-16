package cl.inexcell.exploradorperu.objetos;

import java.util.ArrayList;

/**
 * Created by felip on 18/08/2015.
 */
public class DCTParamFonoCabecera {
    String vendor;
    String dslam;
    String model;
    ArrayList<String> parametrosElectricos;

    public DCTParamFonoCabecera(String vendor, String dslam, String model) {
        this.vendor = vendor;
        this.dslam = dslam;
        this.model = model;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDslam() {
        return dslam;
    }

    public void setDslam(String dslam) {
        this.dslam = dslam;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public ArrayList<String> getParametrosElectricos() {
        return parametrosElectricos;
    }

    public void setParametrosElectricos(ArrayList<String> parametrosElectricos) {
        this.parametrosElectricos = parametrosElectricos;
    }
}
