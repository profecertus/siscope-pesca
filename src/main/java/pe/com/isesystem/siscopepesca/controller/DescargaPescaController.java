package pe.com.isesystem.siscopepesca.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/descarga/v1")
public class DescargaPescaController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/saveDescarga")
    public ResponseEntity<String> saveDescarga(@RequestBody Object descarga){
        mongoTemplate.save(descarga, "descarga-pesca");
        System.out.println(descarga);
        return new ResponseEntity<>("df", HttpStatus.OK);
    }

    @GetMapping("/saludo")
    public String getSaludo() {
        return "¡Hola, mundo!";
    }
}
