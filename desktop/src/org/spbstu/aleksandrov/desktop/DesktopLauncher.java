package org.spbstu.aleksandrov.desktop;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.spbstu.aleksandrov.Tetris;
import org.spbstu.aleksandrov.TetrisDl4j;
import org.spbstu.aleksandrov.model.Tetromino;

public class DesktopLauncher {

	public static boolean NN = true;

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		Game tetris;
		if (NN) tetris = new TetrisDl4j(true, "./nn/network-1684012105630.zip");
		else tetris = new Tetris();
		new LwjglApplication(tetris, config);
	}
}
