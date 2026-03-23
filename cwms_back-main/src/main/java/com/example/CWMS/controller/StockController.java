package com.example.CWMS.controller;

import com.example.CWMS.dto.StockChartDTO;
import com.example.CWMS.dto.StockStatusDTO;
import com.example.CWMS.dto.InventoryScanDTO; // Assurez-vous d'avoir ce DTO
import com.example.CWMS.iservice.IStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin("*")
public class StockController {

    @Autowired private IStockService stockService;

    // Utilisé par le Dashboard Angular
    @GetMapping("/dashboard")
    public List<StockStatusDTO> getWebDashboard() {

        return stockService.getAllStock();
    }

    // Utilisé par la page "Scan History" (Journal d'audit mobile)
    @GetMapping("/scans-history")
    public List<InventoryScanDTO> getHistory() {
        return stockService.getRecentScans(); // Méthode à créer dans votre service
    }

    @GetMapping("/scan")
    public ResponseEntity<List<StockStatusDTO>> handleScan(
            @RequestParam String code,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "Mobile") String device) {

        List<StockStatusDTO> res = stockService.scanProcess(code, userId, device);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/charts")
    public ResponseEntity<StockChartDTO> getCharts() {
        return ResponseEntity.ok(stockService.getChartData());
    }
}