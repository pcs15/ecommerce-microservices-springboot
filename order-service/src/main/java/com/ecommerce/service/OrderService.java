package com.ecommerce.service;

import com.ecommerce.dto.CustomerDto;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Order;
import com.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.customer-url}")
    private String customerServiceUrl;

    @Value("${services.product-url}")
    private String productServiceUrl;

    /**
     * @param authorizationHeader nilai header Authorization asli dari request
     *                            masuk ("Bearer xxx"), diteruskan ke Customer &
     *                            Product Service biar mereka juga bisa validasi
     *                            token + ownership check (defense in depth).
     * @param userId              subject token pemesan, buat ownership order.
     */
    public Order createOrder(OrderRequest request, String authorizationHeader, String userId) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity harus lebih dari 0!");
        }

        // 1. Fetch Customer Data
        CustomerDto customer = webClientBuilder.build().get()
                .uri(customerServiceUrl + "/api/customers/" + request.getCustomerId())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .block();

        if (customer == null) {
            throw new RuntimeException("Customer not found!");
        }

        // 2. Fetch Product Data
        ProductDto product = webClientBuilder.build().get()
                .uri(productServiceUrl + "/api/products/" + request.getProductId())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();

        if (product == null) {
            throw new RuntimeException("Product not found!");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock!");
        }

        // 3. Deduct stock from Product Service
        webClientBuilder.build().put()
                .uri(productServiceUrl + "/api/products/" + request.getProductId()
                        + "/kurangi-stok?qty=" + request.getQuantity())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();

        // 4. Create and Save Order
        try {
            Order order = new Order();
            order.setCustomerId(customer.getId());
            order.setProductId(product.getId());
            order.setQuantity(request.getQuantity());
            order.setTotalPrice(product.getPrice() * request.getQuantity());
            order.setOrderDate(LocalDateTime.now());
            order.setUserId(userId);

            return orderRepository.save(order);
        } catch (Exception e) {
            // Rollback/compensating action: stock sudah kepotong di step 3 tapi
            // order gagal disimpan (mis. Mongo down) - kembalikan stock biar
            // gak hilang permanen.
            restoreStock(request, authorizationHeader);
            throw new RuntimeException("Gagal menyimpan order, stock sudah dikembalikan: " + e.getMessage(), e);
        }
    }

    private void restoreStock(OrderRequest request, String authorizationHeader) {
        webClientBuilder.build().put()
                .uri(productServiceUrl + "/api/products/" + request.getProductId()
                        + "/tambah-stok?qty=" + request.getQuantity())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .block();
    }
}
