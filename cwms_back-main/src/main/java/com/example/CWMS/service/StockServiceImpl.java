package com.example.CWMS.service;

import com.example.CWMS.dto.*;
import com.example.CWMS.iservice.IStockService;
import com.example.CWMS.db2.entities.InventoryScan;
import com.example.CWMS.db2.repositories.InventoryScanRepository;
import com.example.CWMS.db2.repositories.StockStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements IStockService {

    @Autowired private StockStatusRepository stockRepo;
    @Autowired private InventoryScanRepository scanRepo;

    @Override
    public List<StockStatusDTO> getAllStock() {
        try {
            return mapToStockDTO(stockRepo.findAllDetailed());
        } catch (Exception e) {
            System.err.println("Erreur getAllStock: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<StockStatusDTO> scanProcess(String code, Long userId, String device) {
        try {
            List<StockStatusDTO> results = mapToStockDTO(stockRepo.findByCodeOrLotWithDesignation(code));

            InventoryScan scan = new InventoryScan();
            scan.setUserId(userId);
            scan.setScannedCode(code);
            scan.setDeviceInfo(device);
            scan.setScanDate(LocalDateTime.now());

            if (results.isEmpty()) {
                scan.setScanType("NOT_FOUND");
                scan.setWarehouse("N/A");
                scan.setLocation("N/A");
            } else {
                scan.setScanType("STOCK_CHECK");
                scan.setWarehouse(results.get(0).getWarehouse());
                scan.setLocation(results.get(0).getLocation());
            }
            scanRepo.save(scan);
            return results;
        } catch (Exception e) {
            System.err.println("Erreur scanProcess: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<InventoryScanDTO> getRecentScans() {
        try {
            return scanRepo.findAll().stream()
                    .sorted(Comparator.comparing(InventoryScan::getScanDate).reversed())
                    .limit(50)
                    .map(this::mapToScanDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<StockStatusDTO> mapToStockDTO(List<Object[]> data) {
        if (data == null) return new ArrayList<>();
        return data.stream().map(row -> {
            try {
                return new StockStatusDTO(
                        row[0] != null ? row[0].toString().trim() : "N/A",
                        row[1] != null ? row[1].toString().trim() : "Sans Désignation",
                        row[2] != null ? row[2].toString().trim() : "N/A",
                        row[3] != null ? row[3].toString().trim() : "N/A",
                        row[4] != null ? row[4].toString().trim() : "N/A",
                        row[5] != null ? ((Number) row[5]).doubleValue() : 0.0,
                        row[6] != null ? (java.util.Date) row[6] : new java.util.Date()
                );
            } catch (Exception e) { return null; }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private InventoryScanDTO mapToScanDTO(InventoryScan scan) {
        InventoryScanDTO dto = new InventoryScanDTO();
        dto.setId(scan.getId());
        dto.setUserId(scan.getUserId());
        dto.setScannedCode(scan.getScannedCode());
        dto.setScanType(scan.getScanType());
        dto.setWarehouse(scan.getWarehouse());
        dto.setLocation(scan.getLocation());
        dto.setScanDate(scan.getScanDate());
        dto.setDeviceInfo(scan.getDeviceInfo());
        return dto;
    }

    @Override
    public StockChartDTO getChartData() {
        StockChartDTO chart = new StockChartDTO();
        try {
            // Statistiques par magasin
            chart.setWarehouseStats(stockRepo.countByWarehouse().stream()
                    .map(row -> new StockChartDTO.WarehouseStat(
                            row[0] != null ? row[0].toString().trim() : "N/A",
                            row[1] != null ? ((Number) row[1]).longValue() : 0L,
                            row[2] != null ? ((Number) row[2]).doubleValue() : 0.0))
                    .collect(Collectors.toList()));

            // Top emplacements
            chart.setLocationStats(stockRepo.countByLocation().stream()
                    .map(row -> new StockChartDTO.LocationStat(
                            row[0] != null ? row[0].toString().trim() : "N/A",
                            row[1] != null ? row[1].toString().trim() : "N/A",
                            row[2] != null ? ((Number) row[2]).longValue() : 0L))
                    .collect(Collectors.toList()));

            // Distribution corrigée
            List<Object[]> dist = stockRepo.getStockLevelDistribution();
            if (dist != null && !dist.isEmpty()) {
                Object[] r = dist.get(0);
                chart.setStockLevelDist(new StockChartDTO.StockLevelDist(
                        r[0] != null ? ((Number) r[0]).longValue() : 0L,
                        r[1] != null ? ((Number) r[1]).longValue() : 0L,
                        r[2] != null ? ((Number) r[2]).longValue() : 0L));
            }
        } catch (Exception e) {
            System.err.println("Erreur Chart Data : " + e.getMessage());
        }
        return chart;
    }
}