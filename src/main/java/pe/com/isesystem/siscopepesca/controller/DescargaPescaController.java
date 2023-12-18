package pe.com.isesystem.siscopepesca.controller;

import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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
    public ResponseEntity<String> saveGastosEmb(@RequestBody Object descarga){
        mongoTemplate.save(descarga, "gastos-embarcacion");
        return new ResponseEntity<>("OK ", HttpStatus.OK);
    }

    @GetMapping("/getGastosEmb")
    public ResponseEntity<List<DBObject>> getGastosEmb() {
        List<DBObject> documentos = mongoTemplate.findAll(DBObject.class, "gastos-embarcacion");
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }

}
