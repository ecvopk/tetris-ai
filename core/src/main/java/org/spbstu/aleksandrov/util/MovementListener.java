package org.spbstu.aleksandrov.util;

import org.spbstu.aleksandrov.controller.Player;
import org.spbstu.aleksandrov.model.Tetromino;

public class MovementListener {

    Player player;

    public MovementListener(Player player) {
        this.player = player;
    }

    public void takeAction(Tetromino.Movement move) {
        player.takeAction();
    }

}
