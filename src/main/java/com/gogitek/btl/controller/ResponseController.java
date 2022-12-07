package com.gogitek.btl.controller;

import com.gogitek.btl.processing.BusinessResolveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/result")
public class ResponseController {
    @GetMapping()
    public ResponseEntity<?> getResult() {
        ResponseData res = BusinessResolveService
                .getDataForApi();
        return ResponseEntity.ok(res);
    }
}
