package org.example.config;

import org.example.pvp.stats.StatisticsService;
import org.example.pvp.interfaces.MatchService;
import org.example.pvp.interfaces.MatchmakingService;
import org.example.pvp.interfaces.RankingService;
import org.example.pvp.matchmaking.VersusMatchmakingService;
import org.example.pvp.services.VersusEloRatingService;
import org.example.pvp.services.VersusMatchService;
import org.example.pvp.services.VersusRankingService;
import org.example.winnercalculator.FiftyFiftyRandomWinner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DependencyInjectionRepository {

    @Bean
    public MatchmakingService matchmakingService() {
        return new VersusMatchmakingService(statisticsService());
    }

    @Bean
    public RankingService rankingService() {
        return new VersusRankingService();
    }

    @Bean
    public StatisticsService statisticsService() {
        return new StatisticsService(rankingService());
    }

    @Bean
    public MatchService matchService() {
        return new VersusMatchService(new FiftyFiftyRandomWinner(), new VersusEloRatingService(statisticsService()), rankingService());
    }
}
