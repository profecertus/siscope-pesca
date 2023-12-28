package pe.com.isesystem.siscopepesca.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DBObject;
import com.fasterxml.jackson.databind.*;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import pe.com.isesystem.siscopepesca.configuration.RespuestaHttp;

import java.util.List;

@RestController
@RequestMapping("/descarga/v1")
public class DescargaPescaController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/saveDescarga")
    public ResponseEntity<String> saveDescarga(@RequestBody Object descarga){
        mongoTemplate.save(descarga, "descarga-pesca");
        return new ResponseEntity<>("OK ", HttpStatus.OK);
    }

    @GetMapping("/getDescargas")
    public ResponseEntity<List<DBObject>> getDescargas() {
        List<DBObject> documentos = mongoTemplate.findAll(DBObject.class, "descarga-pesca");
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }

    @GetMapping("/getCorrelativo/{anio}")
    public ResponseEntity<DBObject> getCorrelativo(@PathVariable Long anio) {
        Query query = new Query(Criteria.where("anio").is(anio));
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
            Query query = new Query(Criteria.where("embarcacion.idEmbarcacion").
                    is(idEmbarcacion).
                    and("semana.id").
                    is(idSemana).
                    and("idTipoServicio").
                    is(idTipoServicio));

            UpdateResult r = mongoTemplate.replace(query, descarga, "gastos-embarcacion");

            if(r.getMatchedCount() == 0)
                mongoTemplate.insert(descarga, "gastos-embarcacion");

        } catch (JsonProcessingException e) {
            respuestaHttp.setValorDevuelto("Error al convertir el objeto a JSON");
            return ResponseEntity.status(500).body(respuestaHttp);
        }
        return new ResponseEntity<>(respuestaHttp, HttpStatus.OK);
    }

    @GetMapping("/getGastosEmb/{embarcacion}/{semana}/{servicio}")
    public ResponseEntity<List<DBObject>> getGastosEmb(@PathVariable Long embarcacion, @PathVariable Long semana, @PathVariable Long servicio) {
        Query query = new Query(Criteria.where("embarcacion.idEmbarcacion").is(embarcacion).and("semana.id").is(semana).and("idTipoServicio").is(servicio));
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
