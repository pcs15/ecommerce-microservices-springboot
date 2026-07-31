package com.ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    // Katalog produk gak ada ownership per-user, jadi cukup authenticated
    // (role apapun) buat GET, gak perlu ownership check.
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Nambah produk baru ke katalog = tindakan admin/inventory.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        // Paksa create baru - abaikan id yang mungkin dikirim client, biar gak
        // ke-merge/nimpa produk existing.
        product.setId(null);
        return productRepository.save(product);
    }
    // ================== UPDATE PRODUCT ==================
@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{id}")
public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                             @RequestBody Product product) {

    return productRepository.findById(id)
            .map(existing -> {
                existing.setName(product.getName());
                existing.setPrice(product.getPrice());
                existing.setStock(product.getStock());
                existing.setCategory(product.getCategory());

                Product updated = productRepository.save(existing);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
}


    // Dipanggil Order Service atas nama customer pas checkout - ROLE_USER
    // harus tetap bisa akses, jadi TIDAK di-@PreAuthorize ADMIN-only.
    @PutMapping("/{id}/kurangi-stok")
    public ResponseEntity<Product> reduceStock(@PathVariable Long id, @RequestParam Integer qty) {
        if (qty == null || qty <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return productRepository.findById(id).map(product -> {
            if (product.getStock() >= qty) {
                product.setStock(product.getStock() - qty);
                productRepository.save(product);
                return ResponseEntity.ok(product);
            } else {
                return ResponseEntity.badRequest().body(product);
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // Kebalikan kurangi-stok - dipanggil Order Service buat rollback stock
    // kalau create-order gagal setelah stock kepotong. Aksesnya sama kayak
    // kurangi-stok (bagian alur checkout, bukan admin-only).
    @PutMapping("/{id}/tambah-stok")
    public ResponseEntity<Product> restoreStock(@PathVariable Long id, @RequestParam Integer qty) {
        if (qty == null || qty <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return productRepository.findById(id).map(product -> {
            product.setStock(product.getStock() + qty);
            productRepository.save(product);
            return ResponseEntity.ok(product);
        }).orElse(ResponseEntity.notFound().build());
    }

// Hapus produk dari katalog - hanya ADMIN yang boleh menghapus produk.
// Controller akan mencari produk berdasarkan ID terlebih dahulu.
// Jika produk ditemukan, Repository akan menghapus data dari database.
// Jika produk tidak ditemukan, sistem mengembalikan status 404 Not Found.
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

    return productRepository.findById(id)
            .map(product -> {
                productRepository.delete(product);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
}    
    

}


