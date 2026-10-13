package com.nextlevel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nextlevel.dto.SalesPredictionRequest;
import com.nextlevel.dto.SalesPredictionResponse;

@Service
public class PredictionClientService {

    private final RestTemplate restTemplate;
    private final String predictionUrl;

    public PredictionClientService(RestTemplate restTemplate,
            @Value("${app.prediction.url:http://localhost:8000/predecir}") String predictionUrl) {
        this.restTemplate = restTemplate;
        this.predictionUrl = predictionUrl;
    }

    public SalesPredictionResponse predict(SalesPredictionRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SalesPredictionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<SalesPredictionResponse> response =
                restTemplate.postForEntity(predictionUrl, entity, SalesPredictionResponse.class);

        return response.getBody();
    }
}
