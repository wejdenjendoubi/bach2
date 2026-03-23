package com.example.CWMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockStatusDTO {
    private String itemCode;
    private String designation;
    private String warehouse;
    private String location;
    private String lotCode;
    private Double quantity;
    private Date lastMovement;
}