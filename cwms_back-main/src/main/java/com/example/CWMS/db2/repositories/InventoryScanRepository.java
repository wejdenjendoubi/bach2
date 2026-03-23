package com.example.CWMS.db2.repositories;

import com.example.CWMS.db2.entities.InventoryScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryScanRepository extends JpaRepository<InventoryScan, Long> {}