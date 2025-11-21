package biz.atomeo.l9.config;

import biz.atomeo.l9.dto.GameCategory;
import biz.atomeo.l9.dto.GameInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "l9")
@Getter
@Setter
public class L9AppProperties {
    private List<GameCategory> catalogue;

    public Map<String, GameInfo> getGames() {
        return catalogue.stream()
                .flatMap(cat -> cat.games().stream())
                .collect(Collectors.toMap(GameInfo::id,
                        Function.identity(),
                        (existingGameInfo, newGameInfo) -> existingGameInfo));
    }
}
