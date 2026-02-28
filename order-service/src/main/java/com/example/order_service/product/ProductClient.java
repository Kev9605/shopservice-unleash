package com.example.order_service.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.url}")String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Optional<ProductDto> getProductById(Long id) {
        try{
            String url = this.baseUrl + "/api/products/" + id;
            ProductDto productDto = this.restTemplate.getForObject(url, ProductDto.class);
            return Optional.ofNullable(productDto);
        } catch(HttpClientErrorException.NotFound e){
            return Optional.empty();
        }
    }
}
