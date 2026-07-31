package com.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String address;

    private String membershipLevel;
    private Integer poin;

    // FK ke User.id di Auth Service - dipakai buat ownership check.
    private Long userId;

    // Field initializer biasa gak reliable di sini - Jackson deserialize lewat
    // all-args constructor (Lombok @ConstructorProperties), jadi field yang gak
    // dikirim di JSON kepassing null, ngelewatin initializer. @PrePersist jamin
    // default keisi berapa pun cara entity ini dibikin.
    @PrePersist
    void applyDefaults() {
        if (membershipLevel == null) {
            membershipLevel = "BRONZE";
        }
        if (poin == null) {
            poin = 0;
        }
    }
}
