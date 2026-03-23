package com.example.CWMS.iservice;

import com.example.CWMS.dto.InventoryScanDTO;
import com.example.CWMS.dto.StockChartDTO;
import com.example.CWMS.dto.StockStatusDTO;
import java.util.List;

public interface IStockService {
    List<StockStatusDTO>  getAllStock();
    List<StockStatusDTO>  scanProcess(String code, Long userId, String device);
    List<InventoryScanDTO> getRecentScans();
    StockChartDTO         getChartData();
}