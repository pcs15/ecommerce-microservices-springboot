package com.ecommerce.employee.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.employee.entity.ModelAbsensi;

@Repository
public interface AbsensiRepository extends MongoRepository<ModelAbsensi, String> {

    List<ModelAbsensi> findByIdKaryawan(String idKaryawan);

    Optional<ModelAbsensi> findByIdKaryawanAndTanggal(String idKaryawan, LocalDate tanggal);
}
