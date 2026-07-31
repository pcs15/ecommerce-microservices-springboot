package com.ecommerce.employee.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.ecommerce.employee.entity.ModelKaryawan;

@Repository
public interface KaryawanRepository extends MongoRepository<ModelKaryawan, String> {

    Optional<ModelKaryawan> findByEmail(String email);

    Optional<ModelKaryawan> findByUserId(String userId);
}
