package biz.atomeo.l9.config;

import biz.atomeo.l9.dto.GameInfoDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "l9")
@Getter
@Setter
public class L9AppProperties {
    Map<String, GameInfoDTO> games;
}
