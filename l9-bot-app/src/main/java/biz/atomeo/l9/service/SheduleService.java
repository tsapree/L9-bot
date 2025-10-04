package biz.atomeo.l9.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class SheduleService {

    private final LayeredSessionProvider layeredSessionProvider;

    @Value("${l9.session-cache-period-minutes:15}")
    private int minutesBeforeSave;

    @Scheduled(fixedDelay = 60*1000*5)
    private void tick() {
        layeredSessionProvider.parkSessions(minutesBeforeSave);
    }
}
