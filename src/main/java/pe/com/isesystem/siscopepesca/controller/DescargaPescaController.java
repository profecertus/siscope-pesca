package pe.com.isesystem.siscopepesca.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.com.isesystem.siscopepesca.configuration.RespuestaHttp;
import org.apache.logging.log4j.*;
import pe.com.isesystem.siscopepesca.model.GastoDescargaPesca;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RestController
@RequestMapping("/descarga/v1")
public class DescargaPescaController {

    private final MongoTemplate mongoTemplate;
    private static final Logger logger = LogManager.getLogger(DescargaPescaController.class);

    public DescargaPescaController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/getGastos/{tipoServicio}/{embarcacion}/{semana}")
    public ResponseEntity<List<DBObject>> getGastos(@PathVariable Long tipoServicio, @PathVariable Long embarcacion, @PathVariable Long semana) {
        Query miQuery = new Query();
        if(tipoServicio !=0){
            miQuery.addCriteria(where("idTipoServicio").is(tipoServicio));
        }
        if(embarcacion != 0){
            miQuery.addCriteria(where("embarcacion.idEmbarcacion").is(embarcacion));
        }
        if(semana != 0){
            miQuery.addCriteria(where("semana.id").is(semana));
        }
        miQuery.addCriteria(where("semana.estado").is(false));

        List<DBObject> documentos = mongoTemplate.find(
                miQuery,
                DBObject.class,
                "gastos-embarcacion"
        );

        //Ahora debo preguntar si los datos estan llenos y si lo estan si estan pagados

        List<DBObject> docValidos = new ArrayList<>();
        for (DBObject documento : documentos) {
            List<Document> docs = (List<Document>) documento.get("datos");
            List<Document> validos = docs.stream().filter(o -> !o.get("pagado").
                    toString().contains("true") && Double.parseDouble(o.get("total").toString()) != 0).collect(Collectors.toList());
            documento.put("datos", validos);
            if (!validos.isEmpty()) {
                docValidos.add(documento);
            }
        }
        return new ResponseEntity<>(docValidos, HttpStatus.OK);
    }

    @GetMapping("/getGastosDescarga/{tipoServicio}/{embarcacion}/{semana}")
    public ResponseEntity<List<GastoDescargaPesca>> getGastosDescarga(@PathVariable Long tipoServicio, @PathVariable Long embarcacion, @PathVariable Long semana) throws JsonProcessingException {
        Query miQuery = new Query();
        if(tipoServicio !=0){
            //Revisar el tipo
            miQuery.addCriteria(where("idTipoServicio").is(tipoServicio));
        }
        if(embarcacion != 0){
            miQuery.addCriteria(where("embarcacion.idEmbarcacion").is(embarcacion));
        }
        if(semana != 0){
            miQuery.addCriteria(where("semana.id").is(semana));
        }
        miQuery.addCriteria(where("semana.estado").is(false));

        List<DBObject> documentos = mongoTemplate.find(
                miQuery,
                DBObject.class,
                "descarga-pesca"
        );

        //Ahora debo preguntar si los datos estan llenos y si lo estan si estan pagados
        List<GastoDescargaPesca> docValidos = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String descargaJson = objectMapper.writeValueAsString(documentos);
        JsonNode jsonNode = objectMapper.readTree( descargaJson );
        for(JsonNode j : jsonNode){
            //Proveedor de Atraque
            GastoDescargaPesca pCertificado = new GastoDescargaPesca();
            pCertificado.set_id(j.get("_id"));
            pCertificado.setIdMoneda( Integer.parseInt(j.get("monedaCertificado").get("idMoneda").toString()) );
            pCertificado.setPrecio( Float.parseFloat( j.get("precioCertificado").toString().replace("\"","")) );
            pCertificado.setRazonSocial( "GENERICO" );
            pCertificado.setIdProveedor( 0 );
            pCertificado.setTipoServicioNombre("Proveedor de Atraque");
            docValidos.add(pCertificado);

            //Proveedor de Atraque
            GastoDescargaPesca patraque = new GastoDescargaPesca();
            patraque.set_id(j.get("_id"));
            patraque.setIdMoneda( Integer.parseInt(j.get("monedaAtraque").get("idMoneda").toString()) );
            patraque.setPrecio( Float.parseFloat( j.get("precioAtraque").toString().replace("\"","")) );
            patraque.setRazonSocial( "GENERICO" );
            patraque.setIdProveedor( 0 );
            patraque.setTipoServicioNombre("Proveedor de Atraque");
            docValidos.add(patraque);

            //Proveedor de Habilitacion
            GastoDescargaPesca phab = new GastoDescargaPesca();
            phab.set_id(j.get("_id"));
            phab.setIdMoneda( Integer.parseInt(j.get("monedaHabilitacion").get("idMoneda").toString()) );
            phab.setPrecio( Float.parseFloat( j.get("precioHabilitacion").toString().replace("\"","")) );
            phab.setRazonSocial( "GENERICO" );
            phab.setIdProveedor( 0 );
            phab.setTipoServicioNombre("Proveedor de Habilitación");
            docValidos.add(phab);

            //Proveedor de Flete
            GastoDescargaPesca pflete = new GastoDescargaPesca();
            pflete.set_id(j.get("_id"));
            pflete.setIdMoneda( Integer.parseInt(j.get("monedaFlete").get("idMoneda").toString()) );
            pflete.setPrecio( Float.parseFloat( j.get("totalFlete").toString().replace("\"","")) );
            pflete.setRazonSocial( j.get("camara").get("idProveedor").get("razonSocial").toString().replace("\"","") );
            pflete.setIdProveedor( Integer.parseInt(j.get("camara").get("idProveedor").get("idProveedor").toString() ) );
            pflete.setTipoServicioNombre("Proveedor de Flete");
            docValidos.add(pflete);

            //Proveedor de Muelle
            GastoDescargaPesca gdp = new GastoDescargaPesca();
            gdp.set_id(j.get("_id"));
            gdp.setIdMoneda( Integer.parseInt(j.get("monedaMuelle").get("idMoneda").toString()) );
            gdp.setPrecio( Float.parseFloat(j.get("precioMuelle").toString()) );
            gdp.setRazonSocial( j.get("muelle").get("razonSocial").toString().replace("\"","") );
            gdp.setIdProveedor( Integer.parseInt(j.get("muelle").get("idProveedor").toString() ) );
            gdp.setTipoServicioNombre("Proveedor Muelle");
            docValidos.add(gdp);
        }

        return new ResponseEntity<>(docValidos, HttpStatus.OK);
    }

    @PostMapping("/saveDescarga/{accion}")
    public ResponseEntity<String> saveDescarga(@PathVariable String accion, @RequestBody Object descarga) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String descargaJson = objectMapper.writeValueAsString(descarga);
        JsonNode jsonNode = objectMapper.readTree(descargaJson);
        String numTicket = jsonNode.get("numTicket").toString();

        Query consulta = new Query(where("numTicket").is(numTicket.replace("\"", "")));

        if(accion.contains("N")){
            mongoTemplate.save(descarga, "descarga-pesca");
        } else if (accion.contains("M")) {
                mongoTemplate.replace(consulta, descarga, "descarga-pesca");
        }

        return new ResponseEntity<>("OK ", HttpStatus.OK);
    }

    @GetMapping("/buscarArribo/{idEmbarcacion}/{fechaNumero}")
    public ResponseEntity<List<DBObject>> buscarArribo(@PathVariable int idEmbarcacion,@PathVariable int fechaNumero){
        Query miQuery = new Query();
        miQuery.addCriteria(where("embarcacion.idEmbarcacion").is( idEmbarcacion ));
        miQuery.addCriteria(where("fecha.idDia").is( fechaNumero ));

        List<DBObject> documentos = mongoTemplate.find(
                miQuery,
                DBObject.class,
                "descarga-pesca"
        );

        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }

    @PostMapping("/marcarPagado")
    public ResponseEntity<String> marcarPagado(@RequestBody Object objeto) throws  JsonProcessingException{
        int idTipoServicio;
        ObjectMapper objectMapper = new ObjectMapper();
        String valorJson = objectMapper.writeValueAsString(objeto);
        JsonNode jsonNode = objectMapper.readTree(valorJson);

        idTipoServicio = Integer.parseInt(jsonNode.get("tipoServicio").get("idServicio").toString() );
        int finalIdTipoServicio = idTipoServicio;

        jsonNode.get("detalleGasto").forEach(jsonNode1 -> {
            //Busco los registros
            Query miQuery = new Query();
            miQuery.addCriteria(where("idTipoServicio").is( finalIdTipoServicio ));
            miQuery.addCriteria(where("semana.id").is( Integer.parseInt( jsonNode1.get("semana").get("id").toString()) ));
            miQuery.addCriteria(where("embarcacion.idEmbarcacion").is( Integer.parseInt( jsonNode1.get("embarcacion").get("idEmbarcacion").toString()) ));

            List<DBObject> documentos = mongoTemplate.find(
                    miQuery,
                    DBObject.class,
                    "gastos-embarcacion"
            );

            documentos.forEach( dbObject -> {
                try {
                    ObjectMapper objectMapper1 = new ObjectMapper();
                    String valorJson1 = objectMapper1.writeValueAsString(dbObject.get("datos"));
                    JsonNode jsonNode3 = objectMapper1.readTree(valorJson1);

                    jsonNode3.forEach( jsonNode2 -> {
                        if (jsonNode2.get("idDia").toString().contains( jsonNode1.get("idDia").toString())){
                            ((ObjectNode) jsonNode2).put("pagado", true);
                        }
                    });

                    List<DBObject> totalDias = new ArrayList<>();
                    // Recorrer las claves del JsonNode y agregar al DBObject
                    for(int i = 0; i < 7; i++){
                        BasicDBObject dbObject5 = new BasicDBObject();
                        jsonNode3.get(i).fields().forEachRemaining(entry -> {
                            String key = entry.getKey();
                            Object value = objectMapper.convertValue(entry.getValue(), Object.class);
                            dbObject5.put(key, value);
                        });
                        totalDias.add(dbObject5);
                    }

                    dbObject.put("datos", totalDias);
                }catch(Exception e){
                    logger.error(e.getMessage());
                }

            });
            documentos.forEach(dbObject -> {
                Query q = new Query();
                q.addCriteria(where("_id").is(dbObject.get("_id")));
                mongoTemplate.replace(q,dbObject, "gastos-embarcacion");
            } );


        } );
        return new ResponseEntity<>("OK ", HttpStatus.OK);
    }

    @GetMapping("/getDescargas")
    public ResponseEntity<List<DBObject>> getDescargas() {
        List<DBObject> documentos = mongoTemplate.findAll(DBObject.class, "descarga-pesca");
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }

    @GetMapping("/getCorrelativo/{anio}")
    public ResponseEntity<DBObject> getCorrelativo(@PathVariable Long anio) {
        Query query = new Query(where("anio").is(anio));
        Update update = new Update().inc("corre", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        DBObject documento = mongoTemplate.findAndModify(
                query,
                update,
                options,
                DBObject.class,
                "correlativo"
        );

        return new ResponseEntity<>(documento, HttpStatus.OK);
    }

    @PostMapping("/saveGastosEmb")
    public ResponseEntity<RespuestaHttp> saveGastosEmb(@RequestBody Object descarga){
        ObjectMapper objectMapper = new ObjectMapper();
        RespuestaHttp respuestaHttp = new RespuestaHttp("OK");
        try {
            String descargaJson = objectMapper.writeValueAsString(descarga);
            JsonNode jsonNode = objectMapper.readTree(descargaJson);
            Long idTipoServicio = Long.parseLong(jsonNode.get("idTipoServicio").toString());
            Long idEmbarcacion = Long.parseLong(jsonNode.get("embarcacion").get("idEmbarcacion").toString());
            Long idSemana = Long.parseLong(jsonNode.get("semana").get("id").toString());
            Query query = new Query(where("embarcacion.idEmbarcacion").
                    is(idEmbarcacion).
                    and("semana.id").
                    is(idSemana).
                    and("idTipoServicio").
                    is(idTipoServicio));

            UpdateResult r = mongoTemplate.replace(query, descarga, "gastos-embarcacion");

            if(r.getMatchedCount() == 0)
                mongoTemplate.insert(descarga, "gastos-embarcacion");

        } catch (JsonProcessingException e) {
            respuestaHttp.setRespuesta("Error al convertir el objeto a JSON");
            return ResponseEntity.status(500).body(respuestaHttp);
        }
        return new ResponseEntity<>(respuestaHttp, HttpStatus.OK);
    }

    @DeleteMapping("/eliminarGastosEmb/{embarcacion}/{semana}/{servicio}")
    public ResponseEntity<List<DBObject>> eliminarGastosEmb(@PathVariable Long embarcacion, @PathVariable Long semana, @PathVariable Long servicio) {
        Query query = new Query(where("embarcacion.idEmbarcacion").is(embarcacion).and("semana.id").is(semana).and("idTipoServicio").is(servicio));
        List<DBObject> documentos = mongoTemplate.findAllAndRemove(
                query,
                DBObject.class,
                "gastos-embarcacion"
        );
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }


    @GetMapping("/getGastosEmb/{embarcacion}/{semana}/{servicio}")
    public ResponseEntity<List<DBObject>> getGastosEmb(@PathVariable Long embarcacion, @PathVariable Long semana, @PathVariable Long servicio) {
        Query query = new Query(where("embarcacion.idEmbarcacion").is(embarcacion).and("semana.id").is(semana).and("idTipoServicio").is(servicio));
        List<DBObject> documentos = mongoTemplate.find(
                query,
                DBObject.class,
                "gastos-embarcacion"
        );
        //List<DBObject> documentos = mongoTemplate.findAll(DBObject.class, "gastos-embarcacion");
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }

    @GetMapping("/getAllGastosEmb")
    public ResponseEntity<List<DBObject>> getAllGastosEmb() {
        List<DBObject> documentos = mongoTemplate.findAll(DBObject.class, "gastos-embarcacion");
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }

}
