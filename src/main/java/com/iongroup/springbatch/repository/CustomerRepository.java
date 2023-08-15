package com.iongroup.springbatch.repository;

import com.iongroup.springbatch.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
