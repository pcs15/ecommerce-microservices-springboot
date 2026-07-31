package com.ecommerce.employee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.employee.entity.ModelAbsensi;
import com.ecommerce.employee.entity.ModelKaryawan;
import com.ecommerce.employee.repository.AbsensiRepository;
import com.ecommerce.employee.repository.KaryawanRepository;

@RestController
@RequestMapping("/api/absensi")
public class AbsensiController {

    @Autowired
    private AbsensiRepository absensiRepo;

    @Autowired
    private KaryawanRepository karyawanRepo;

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    // Karyawan absen buat dirinya sendiri - idKaryawan & userId diambil dari
    // token, BUKAN dari body, biar gak bisa absen atas nama karyawan lain.
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping
    public ResponseEntity<?> tambahAbsen(@RequestBody ModelAbsensi absensi, Authentication authentication) {
        ModelKaryawan me = karyawanRepo.findByUserId(authentication.getName()).orElse(null);
        if (me == null) {
            return ResponseEntity.badRequest().body("Belum ada data karyawan buat user ini.");
        }
        // Paksa create baru - abaikan id yang mungkin dikirim client, biar gak
        // ke-replace dokumen existing punya karyawan lain.
        absensi.setId(null);
        absensi.setIdKaryawan(me.getId());
        absensi.setUserId(authentication.getName());
        return ResponseEntity.ok(absensiRepo.save(absensi));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ModelAbsensi> getAllAbsensi() {
        return absensiRepo.findAll();
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/karyawan/{idKaryawan}")
    public ResponseEntity<?> getAbsenByKaryawan(@PathVariable String idKaryawan, Authentication authentication) {
        if (!isAdmin(authentication)) {
            ModelKaryawan me = karyawanRepo.findByUserId(authentication.getName()).orElse(null);
            if (me == null || !me.getId().equals(idKaryawan)) {
                return ResponseEntity.status(403).body("Gak boleh lihat absensi karyawan lain.");
            }
        }
        return ResponseEntity.ok(absensiRepo.findByIdKaryawan(idKaryawan));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAbsensi(@PathVariable String id) {
        if (absensiRepo.existsById(id)) {
            absensiRepo.deleteById(id);
            return ResponseEntity.ok("Absensi dengan ID " + id + " berhasil dihapus.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAbsensi(@PathVariable String id, @RequestBody ModelAbsensi updatedAbsensi,
            Authentication authentication) {
        return absensiRepo.findById(id).<ResponseEntity<?>>map(absensi -> {
            if (!isAdmin(authentication) && !authentication.getName().equals(absensi.getUserId())) {
                return ResponseEntity.status(403).body("Gak boleh ubah absensi karyawan lain.");
            }
            absensi.setTanggal(updatedAbsensi.getTanggal());
            absensi.setJamMasuk(updatedAbsensi.getJamMasuk());
            absensi.setJamPulang(updatedAbsensi.getJamPulang());
            absensi.setStatus(updatedAbsensi.getStatus());
            // idKaryawan & userId tidak diubah, tetap sesuai data awal
            return ResponseEntity.ok(absensiRepo.save(absensi));
        }).orElse(ResponseEntity.notFound().build());
    }

}
