package pe.com.isesystem.siscopepesca.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GastoDescargaPesca {
    public Object _id;
    public int idMoneda;
    public int idProveedor;
    public double precio;
    public String razonSocial;
    public String tipoServicioNombre;
}
