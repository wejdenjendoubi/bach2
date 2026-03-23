package com.example.CWMS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockChartDTO {

    private List<WarehouseStat>  warehouseStats;
    private List<LocationStat>   locationStats;
    private StockLevelDist       stockLevelDist;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class WarehouseStat {
        private String warehouse;
        private Long   nbArticles;
        private Double totalQty;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class LocationStat {
        private String warehouse;
        private String location;
        private Long   nbArticles;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class StockLevelDist {
        private Long optimal;
        private Long low;
        private Long critical;
    }
}