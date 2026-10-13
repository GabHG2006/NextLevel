package com.nextlevel.controller;

import org.springframework.web.bind.annotation.*;

import com.nextlevel.dto.SalesPredictionRequest;
import com.nextlevel.dto.SalesPredictionResponse;
import com.nextlevel.service.PredictionClientService;

@RestController
@RequestMapping("/api/predictivo")
public class PredictionController {

    private final PredictionClientService predictionService;

    public PredictionController(PredictionClientService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/ventas")
    public SalesPredictionResponse predecirVentas(@RequestBody SalesPredictionRequest request) {
        return predictionService.predict(request);
    }
}
