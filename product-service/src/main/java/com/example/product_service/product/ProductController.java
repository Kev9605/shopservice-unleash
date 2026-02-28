package com.example.product_service.product;

import com.example.product_service.service.FeatureFlagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final FeatureFlagService featureFlagService;

    @Autowired
    public ProductController(ProductRepository productRepository, FeatureFlagService featureFlagService) {
        this.productRepository = productRepository;
        this.featureFlagService = featureFlagService;
    }

    @GetMapping
    public List<Product> findAll() {

        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        return productRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // NEW: Premium endpoint (10% discount when flag ON)
    @GetMapping("/premium")
    public List<Product> findAllPremium() {
        boolean discountOn = featureFlagService.premiumPricingEnabled();

        return productRepository.findAll().stream().map(p -> {
            if (!discountOn) return p;

            // Copy to avoid mutating JPA entity state
            Product copy = new Product();
            copy.setId(p.getId());
            copy.setName(p.getName());
            copy.setQuantity(p.getQuantity());

            double discounted = Math.round((p.getPrice() * 0.90) * 100.0) / 100.0;
            copy.setPrice(discounted);

            return copy;
        }).toList();
    }

    @PostMapping
    public ResponseEntity<Product> save(@Valid @RequestBody Product product) {
        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
