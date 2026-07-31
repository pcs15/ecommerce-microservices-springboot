package com.ecommerce.employee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.employee.entity.ModelKaryawan;
import com.ecommerce.employee.repository.KaryawanRepository;

@RestController
@RequestMapping("/api/karyawan")
public class KaryawanController {

    @Autowired
    private KaryawanRepository karyawanRepo;

    // Bikin record karyawan baru = tindakan HR, jadi ADMIN-only.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ModelKaryawan tambahKaryawan(@RequestBody ModelKaryawan karyawan) {
        // Paksa create baru - abaikan id yang mungkin dikirim client, biar gak
        // ke-replace record existing.
        karyawan.setId(null);
        return karyawanRepo.save(karyawan);
    }

    // Lihat semua data karyawan = ADMIN-only. Karyawan biasa pakai /me.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ModelKaryawan> getAllKaryawan() {
        return karyawanRepo.findAll();
    }

    // Karyawan (ROLE_EMPLOYEE) lihat profil sendiri lewat userId di token.
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<ModelKaryawan> getMyProfile(Authentication authentication) {
        return karyawanRepo.findByUserId(authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ModelKaryawan updateKaryawan(@PathVariable String id, @RequestBody ModelKaryawan dataBaru) {
        return karyawanRepo.findById(id).map(karyawan -> {
            karyawan.setNama(dataBaru.getNama());
            karyawan.setEmail(dataBaru.getEmail());
            karyawan.setJabatan(dataBaru.getJabatan());
            return karyawanRepo.save(karyawan);
        }).orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String hapusKaryawan(@PathVariable String id) {
        karyawanRepo.deleteById(id);
        return "Karyawan dengan ID " + id + " berhasil dihapus.";
    }

}
