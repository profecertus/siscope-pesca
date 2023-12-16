package pe.com.isesystem.siscopepesca.controller;

import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
