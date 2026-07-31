package com.ecommerce.employee.entity;

import org.springframework.data.annotation.Id;

public class ModelKaryawan {

    @Id
    private String id;
    private String nama;
    private String email;
    private String jabatan;
    // FK ke User.id di Auth Service - dipakai buat ownership check (bukan auth
    // sendiri, login karyawan sekarang lewat Auth Service terpusat).
    private String userId;

    public ModelKaryawan(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
