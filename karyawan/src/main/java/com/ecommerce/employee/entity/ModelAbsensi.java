package com.ecommerce.employee.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.annotation.Id;

public class ModelAbsensi {

    @Id
    private String id;
    private LocalDate tanggal;
    private LocalTime jamMasuk;
    private LocalTime jamPulang;
    private String status;
    private String idKaryawan;
    // FK ke User.id di Auth Service - dipakai buat ownership check.
    private String userId;

    public ModelAbsensi () {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public LocalTime getJamMasuk() {
        return jamMasuk;
    }

    public void setJamMasuk(LocalTime jamMasuk) {
        this.jamMasuk = jamMasuk;
    }

    public LocalTime getJamPulang() {
        return jamPulang;
    }

    public void setJamPulang(LocalTime jamPulang) {
        this.jamPulang = jamPulang;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
