package com.main.eureka.api.controller;

import com.main.eureka.api.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    @GetMapping("/connect")
    public ResponseEntity<Response<Void>> connect() {
        return ResponseEntity.ok(Response.payload(true, "connect to client 1"));
    }
}
