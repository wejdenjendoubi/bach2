package com.example.CWMS.db2.repositories;

import com.example.CWMS.db2.entities.StockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockStatusRepository extends JpaRepository<StockStatus, Long> {

    // REMPLACEZ 'baandbo' par le schéma correct vu dans SSMS
    // REMPLACEZ 'ERPLNBD' par le nom de votre base de données Infor LN si elle est différente

    @Query(value = "SELECT s.t_item, i.t_dsca, s.t_cwar, s.t_loca, s.t_clot, s.t_qhnd, s.t_trdt " +
            "FROM baandbo.ttwhinr1401200 s " +
            "LEFT JOIN baandbo.tttcibd001120 i ON s.t_item = i.t_item " +
            "WHERE s.t_item = :code OR s.t_clot = :code", nativeQuery = true)
    List<Object[]> findByCodeOrLotWithDesignation(@Param("code") String code);

    @Query(value = "SELECT TOP 500 s.t_item, i.t_dsca, s.t_cwar, s.t_loca, s.t_clot, s.t_qhnd, s.t_trdt " +
            "FROM baandbo.ttwhinr1401200 s " +
            "LEFT JOIN baandbo.tttcibd001120 i ON s.t_item = i.t_item " +
            "ORDER BY s.t_trdt DESC", nativeQuery = true)
    List<Object[]> findAllDetailed();

    @Query(value = "SELECT s.t_cwar, COUNT(s.t_item), SUM(s.t_qhnd) " +
            "FROM baandbo.ttwhinr1401200 s GROUP BY s.t_cwar", nativeQuery = true)
    List<Object[]> countByWarehouse();

    @Query(value = "SELECT TOP 10 s.t_cwar, s.t_loca, COUNT(s.t_item) " +
            "FROM baandbo.ttwhinr1401200 s GROUP BY s.t_cwar, s.t_loca " +
            "ORDER BY COUNT(s.t_item) DESC", nativeQuery = true)
    List<Object[]> countByLocation();

    @Query(value = "SELECT " +
            "SUM(CASE WHEN t_qhnd <= 10 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t_qhnd > 10 AND t_qhnd <= 100 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN t_qhnd > 100 THEN 1 ELSE 0 END) " +
            "FROM baandbo.ttwhinr1401200", nativeQuery = true)
    List<Object[]> getStockLevelDistribution();
}