package com.ecommerce.employee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.employee.entity.ModelIzinCuti;
import com.ecommerce.employee.entity.ModelKaryawan;
import com.ecommerce.employee.repository.IzinCutiRepository;
import com.ecommerce.employee.repository.KaryawanRepository;

@RestController
@RequestMapping("/api/izin-cuti")
public class IzinCutiController {

    @Autowired
    private IzinCutiRepository izinRepo;

    @Autowired
    private KaryawanRepository karyawanRepo;

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    // Karyawan ajukan izin buat dirinya sendiri - idKaryawan & userId dari
    // token, BUKAN dari body.
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PostMapping
    public ResponseEntity<?> ajukanIzin(@RequestBody ModelIzinCuti izin, Authentication authentication) {
        ModelKaryawan me = karyawanRepo.findByUserId(authentication.getName()).orElse(null);
        if (me == null) {
            return ResponseEntity.badRequest().body("Belum ada data karyawan buat user ini.");
        }
        // Paksa create baru - abaikan id yang mungkin dikirim client, biar gak
        // ke-replace dokumen existing punya karyawan lain.
        izin.setId(null);
        izin.setIdKaryawan(me.getId());
        izin.setUserId(authentication.getName());
        if (izin.getStatusApproval() == null) {
            izin.setStatusApproval("PENDING");
        }
        return ResponseEntity.ok(izinRepo.save(izin));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ModelIzinCuti> getAllIzin() {
        return izinRepo.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public List<ModelIzinCuti> getIzinByStatus(@RequestParam String status) {
        return izinRepo.findByStatusApproval(status);
    }

    // Approve/reject izin = keputusan HR, ADMIN-only.
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/approval")
    public ModelIzinCuti approveIzin(@PathVariable String id, @RequestParam String status) {
        return izinRepo.findById(id).map(izin -> {
            izin.setStatusApproval(status);
            return izinRepo.save(izin);
        }).orElse(null);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIzin(@PathVariable String id, @RequestBody ModelIzinCuti updatedIzin,
            Authentication authentication) {
        return izinRepo.findById(id).<ResponseEntity<?>>map(izin -> {
            if (!isAdmin(authentication) && !authentication.getName().equals(izin.getUserId())) {
                return ResponseEntity.status(403).body("Gak boleh ubah izin karyawan lain.");
            }
            izin.setJenisIzin(updatedIzin.getJenisIzin());
            izin.setTanggalMulai(updatedIzin.getTanggalMulai());
            izin.setTanggalSelesai(updatedIzin.getTanggalSelesai());
            izin.setAlasan(updatedIzin.getAlasan());
            return ResponseEntity.ok(izinRepo.save(izin));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteIzin(@PathVariable String id) {
        if (izinRepo.existsById(id)) {
            izinRepo.deleteById(id);
            return ResponseEntity.ok("Izin cuti dengan ID " + id + " berhasil dihapus.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
