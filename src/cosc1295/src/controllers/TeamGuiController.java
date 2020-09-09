package cosc1295.src.controllers;

import cosc1295.src.views.gui.TeamGuiView;

public class TeamGuiController {

	/**
	 * Method just launches the GUI.
	 * Read classes TeamGuiView, ContentInflator in package src.views.gui for details.
	 * Read the activity classes in package src.controllers.activities for details.
	 */
	public void runGuiTeamManagementFeatures() {
		TeamGuiView teamGui;
		teamGui = new TeamGuiView();

		teamGui.launchGuiWindow();
	}
}
