package biz.atomeo.l9.bot;

import biz.atomeo.l9.constants.L9Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class L9GameState {
    // Состояние игры, для каждого пользователя бота будет по одному состоянию на каждую игру
    // (возможно, либо внутри либо в отдельном классе - сохранения для игры
    private L9Game l9game;
}
