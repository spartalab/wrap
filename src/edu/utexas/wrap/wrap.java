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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class wrap extends Application{

	public static void main(String[] args) {
		
		launch(args);
		//Get a path to the project file (a Properties file of arbitrary extension)
//		if (args.length < 1) {
//			System.err.println("No model input file supplied");
//			System.exit(1);
//		}
//		projFile = Paths.get(args[0]);
//		
//		
//		//Load the project from the given path
//		try {
//			proj = new Project(projFile);
//		} catch (IOException e) {
//			System.err.println("Error loading project properties");
//			e.printStackTrace();
//			proj = null;
//			System.exit(-1);
//		}
//		
//		proj.run();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		
		// TODO Auto-generated method stub
		primaryStage.setTitle("wrap");
		URL url = getClass().getResource("/edu/utexas/wrap/gui/wrapConfig.fxml");
	
		
		Scene scene  = FXMLLoader.load(url);
		
//		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		
	}

}
