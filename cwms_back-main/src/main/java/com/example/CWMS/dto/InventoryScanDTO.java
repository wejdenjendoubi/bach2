package com.example.CWMS.dto;

import java.time.LocalDateTime;

public class InventoryScanDTO {
    private Long id;
    private Long userId;
    private String scannedCode;
    private String scanType;
    private String warehouse;
    private String location;
    private LocalDateTime scanDate;
    private String deviceInfo;

    // Constructeur sans paramètres
    public InventoryScanDTO() {}

    // Constructeur complet (utile pour les requêtes JPQL ou Native)
    public InventoryScanDTO(Long id, Long userId, String scannedCode, String scanType,
                            String warehouse, String location, LocalDateTime scanDate, String deviceInfo) {
        this.id = id;
        this.userId = userId;
        this.scannedCode = scannedCode;
        this.scanType = scanType;
        this.warehouse = warehouse;
        this.location = location;
        this.scanDate = scanDate;
        this.deviceInfo = deviceInfo;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getScannedCode() { return scannedCode; }
    public void setScannedCode(String scannedCode) { this.scannedCode = scannedCode; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getScanDate() { return scanDate; }
    public void setScanDate(LocalDateTime scanDate) { this.scanDate = scanDate; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
}