/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.utexas.wrap;

import java.net.URL;

import edu.utexas.wrap.gui.ConfigController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.application.Application;
import javafx.event.EventHandler;

public class wrap extends Application{

	public static void main(String[] args) {
		
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		
		// TODO Auto-generated method stub
		primaryStage.setTitle("wrap");
		URL url = getClass().getResource("/edu/utexas/wrap/gui/wrapConfig.fxml");
	
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(url);
		Scene scene  = loader.load();
		
		ConfigController controller = loader.getController();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				// TODO Auto-generated method stub
				controller.exit(arg0);
				arg0.consume();
			}
			
		});

		
//		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		
	}

}
