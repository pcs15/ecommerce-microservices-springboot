package com.ecommerce.employee.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.employee.entity.ModelIzinCuti;

@Repository
public interface IzinCutiRepository extends MongoRepository<ModelIzinCuti, String> {

    List<ModelIzinCuti> findByStatusApproval(String status);

}
