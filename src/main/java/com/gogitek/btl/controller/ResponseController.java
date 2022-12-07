package com.gogitek.btl.controller;

import com.gogitek.btl.processing.BusinessResolveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/result")
public class ResponseController {
    @GetMapping()
    public ResponseEntity<?> getResult(){
        return ResponseEntity.ok(BusinessResolveService.getDataForApi());
    }
}
