package com.example.CWMS.db2.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
@Table(name = "dbo_twhinr1401200", schema = "dbo")
public class StockStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stockage")
    private Long id;

    @Column(name = "t_item")
    private String itemCode;

    @Column(name = "t_cwar")
    private String warehouse;

    @Column(name = "t_loca")
    private String location;

    @Column(name = "t_clot")
    private String lotCode;

    @Column(name = "t_qhnd")
    private Double quantity;

    @Column(name = "t_trdt")
    private Date lastMovement;
}