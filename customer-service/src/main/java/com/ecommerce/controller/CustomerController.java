package com.ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.entity.Customer;
import com.ecommerce.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    // Lihat semua customer = ADMIN-only.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id, Authentication authentication) {
        return customerRepository.findById(id).<ResponseEntity<?>>map(customer -> {
            if (!isAdmin(authentication) && !authentication.getName().equals(String.valueOf(customer.getUserId()))) {
                return ResponseEntity.status(403).body("Gak boleh lihat data customer lain.");
            }
            return ResponseEntity.ok(customer);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Customer daftar profil sendiri - userId diambil dari token, BUKAN dari
    // body, biar gak bisa bikin profil atas nama user lain.
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer, Authentication authentication) {
        // Paksa create baru - abaikan id yang mungkin dikirim client, biar gak
        // ke-merge/nimpa record existing punya orang lain.
        customer.setId(null);
        customer.setUserId(Long.valueOf(authentication.getName()));
        return customerRepository.save(customer);
    }

    // ================== DELETE CUSTOMER ==================
// Menghapus data customer berdasarkan ID.
// ADMIN dapat menghapus customer siapa saja.
// USER hanya dapat menghapus data customer miliknya sendiri.
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteCustomer(@PathVariable Long id,
                                        Authentication authentication) {

    return customerRepository.findById(id)
            .<ResponseEntity<?>>map(customer -> {

                // Jika bukan ADMIN dan mencoba menghapus data customer lain,
                // maka akses ditolak.
                if (!isAdmin(authentication)
                        && !authentication.getName().equals(String.valueOf(customer.getUserId()))) {
                    return ResponseEntity.status(403)
                            .body("Gak boleh menghapus data customer lain.");
                }

                // Hapus data customer dari database.
                customerRepository.delete(customer);

                return ResponseEntity.ok("Customer berhasil dihapus.");
            })
            .orElse(ResponseEntity.notFound().build());
}

    

}
