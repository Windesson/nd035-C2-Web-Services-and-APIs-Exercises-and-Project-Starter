package com.udacity.pricing.repository;

import com.udacity.pricing.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface PriceRepository extends JpaRepository<Price, Long> {

}
