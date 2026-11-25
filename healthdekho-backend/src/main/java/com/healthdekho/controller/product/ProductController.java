
package com.healthdekho.controller.product;

import com.healthdekho.dto.product.InsuranceProductResponse;
import com.healthdekho.service.product.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Get all active products
    @GetMapping
    public ResponseEntity<List<InsuranceProductResponse>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllActiveProducts()
        );
    }

    // Get product details by ID
    @GetMapping("/{productId}")
    public ResponseEntity<InsuranceProductResponse> getProductById(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                productService.getProductById(productId)
        );
    }

    // Get products not already registered by the logged-in customer
   @GetMapping("/available")
public ResponseEntity<List<InsuranceProductResponse>> getAvailableProducts(
        @AuthenticationPrincipal UserDetails userDetails
) {

    return ResponseEntity.ok(
            productService.getAvailableProducts(
                    userDetails.getUsername()
            )
    );
}

    private Long getCustomerId(UserDetails userDetails) {
        // Customer ID lookup will be implemented in the next refinement.
        throw new UnsupportedOperationException(
                "Customer ID lookup is not implemented yet."
        );
    }

    
}
