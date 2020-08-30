package cosc1295.src.controllers;

import cosc1295.src.views.gui.TeamGuiView;

public class TeamGuiController {

	public void runGuiTeamManagementFeatures() {
		TeamGuiView teamGui;
		teamGui = new TeamGuiView();

		teamGui.launchGuiWindow();
	}
}
