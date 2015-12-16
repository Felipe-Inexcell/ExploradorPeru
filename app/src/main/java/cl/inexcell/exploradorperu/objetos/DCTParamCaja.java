package cl.inexcell.exploradorperu.objetos;

/**
 * Created by felip on 18/08/2015.
 */
public class DCTParamCaja {
    int id;
    String proveedor;
    String dslam;
    String velocidad;

    public DCTParamCaja(String id, String proveedor, String dslam, String velocidad) {
        setId(id);
        this.proveedor = proveedor;
        this.dslam = dslam;
        this.velocidad = velocidad;
    }

    public int getId() {
        return id;
    }

    public void setId(String id) {
        if (id.equals(""))
            this.id = -1;
        else
            this.id = Integer.parseInt(id);
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getDslam() {
        return dslam;
    }

    public void setDslam(String dslam) {
        this.dslam = dslam;
    }

    public String getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(String velocidad) {
        this.velocidad = velocidad;
    }
}
