package biz.atomeo.l9.delegators;

import biz.atomeo.l9.openapi.api.L9botApiDelegate;
import biz.atomeo.l9.openapi.model.L9StatRs;
import biz.atomeo.l9.openapi.model.L9StatusRs;
import biz.atomeo.l9.service.BotStateProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class L9BotDelegate implements L9botApiDelegate {
    private final BotStateProvider botStateProvider;

    @Override
    public ResponseEntity<L9StatRs> stats() {
        L9StatRs rs = new L9StatRs()
                .activeUsers(botStateProvider.getActiveUsers())
                .botStatus(botStateProvider.getStatus());
        return ResponseEntity.ok(rs);
    }

    @Override
    public ResponseEntity<L9StatusRs> enableBot() {
        botStateProvider.enableBot();
        return ResponseEntity.ok(new L9StatusRs()
                .status(L9StatusRs.StatusEnum.SUCCESS));
    }

    @Override
    public ResponseEntity<L9StatusRs> disableBot() {
        botStateProvider.disableBot();
        return ResponseEntity.ok(new L9StatusRs()
                .status(L9StatusRs.StatusEnum.SUCCESS));
    }
}
