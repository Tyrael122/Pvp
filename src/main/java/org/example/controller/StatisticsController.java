package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.pvp.stats.StatisticsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public Map<Object, Object> getRankingStatistics() {
        return statisticsService.getStatistics();
    }
}
