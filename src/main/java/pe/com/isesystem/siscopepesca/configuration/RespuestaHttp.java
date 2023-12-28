package pe.com.isesystem.siscopepesca.configuration;

public class RespuestaHttp {
    private  String respuesta;

    public  RespuestaHttp(String respuesta){
        this.respuesta = respuesta;
    }
    public String getRespuesta() {
        return respuesta;
    }

    public void  setRespuesta(String respuesta){
        this.respuesta = respuesta;
    }
}
