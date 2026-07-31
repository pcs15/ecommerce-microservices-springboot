package com.ecommerce.employee.entity;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;

public class ModelIzinCuti {

    @Id
    private String id;
    private String jenisIzin;
    private LocalDate tanggalMulai;
    private LocalDate tanggalSelesai;
    private String alasan;
    private String statusApproval;
    private String idKaryawan;
    // FK ke User.id di Auth Service - dipakai buat ownership check.
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJenisIzin() {
        return jenisIzin;
    }

    public void setJenisIzin(String jenisIzin) {
        this.jenisIzin = jenisIzin;
    }

    public LocalDate getTanggalMulai() {
        return tanggalMulai;
    }

    public void setTanggalMulai(LocalDate tanggalMulai) {
        this.tanggalMulai = tanggalMulai;
    }

    public LocalDate getTanggalSelesai() {
        return tanggalSelesai;
    }

    public void setTanggalSelesai(LocalDate tanggalSelesai) {
        this.tanggalSelesai = tanggalSelesai;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }

    public String getStatusApproval() {
        return statusApproval;
    }

    public void setStatusApproval(String statusApproval) {
        this.statusApproval = statusApproval;
    }

    public String getIdKaryawan() {
        return idKaryawan;
    }

    public void setIdKaryawan(String idKaryawan) {
        this.idKaryawan = idKaryawan;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
