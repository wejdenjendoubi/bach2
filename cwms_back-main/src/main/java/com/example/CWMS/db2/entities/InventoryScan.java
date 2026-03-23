package com.example.CWMS.db2.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "InventoryScans", schema = "dbo")
public class InventoryScan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "scanned_code")
    private String scannedCode;

    private String scanType;
    private String warehouse;
    private String location;
    private LocalDateTime scanDate = LocalDateTime.now();
    private String deviceInfo;
}