package cosc1295.src.controllers;

import cosc1295.src.views.gui.TeamGuiView;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;

import java.io.IOException;

public class TeamGuiController {

	public void runGuiTeamManagementFeatures() {
		TeamGuiView teamGui;
		try {
			teamGui = new TeamGuiView();
			teamGui.prepareActivities();

			teamGui.setupGuiWindow();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
