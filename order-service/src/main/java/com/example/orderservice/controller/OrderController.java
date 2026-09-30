package com.example.orderservice.controller;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private ProductClient productClient;

    @GetMapping
    public List<Order> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
    public ResponseEntity<?> create(@RequestBody Order order) {
        Map<String, Object> product = productClient.getProductById(order.getProductId());
        
        if (product == null || product.get("price") == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Produto nao encontrado");
        }
        
        Double price = Double.valueOf(product.get("price").toString());
        order.setTotalPrice(price * order.getQuantity());
        
        return ResponseEntity.ok(repository.save(order));
    }

    public ResponseEntity<?> createOrderFallback(Order order, Throwable t) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("O serviço de catalogo está indisponível, nao e possível criar o pedido");
    }
}