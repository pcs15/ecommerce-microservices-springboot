package com.ecommerce.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String id;
    private Long customerId;
    private Long productId;
    private Integer quantity;
    private Double totalPrice;
    private LocalDateTime orderDate;

    // FK ke User.id di Auth Service (pemesan) - dipakai buat ownership check.
    private String userId;
}
