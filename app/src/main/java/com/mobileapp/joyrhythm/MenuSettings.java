package com.mobileapp.joyrhythm;

import android.os.Bundle;
import android.preference.*;

import com.mobileapp.joyrhythm.tools.Tools;
import com.mobileapp.joyrhythm.tools.ToolsTracker;

public class MenuSettings extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Tools.setContext(this);
		ToolsTracker.info("Opened settings");
		addPreferencesFromResource(R.xml.settings);
		this.setTitle(Tools.getString(R.string.MenuSettings_title));
	}
}