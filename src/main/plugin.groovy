import activitytracker.ActivityTracker
import activitytracker.ActivityTrackerPlugin
import activitytracker.PluginUI
import activitytracker.TrackerLog
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.PathManager

import static liveplugin.PluginUtil.invokeOnEDT
import static liveplugin.PluginUtil.show

// add-to-classpath $PLUGIN_PATH/bin/production/activity-tracker/
// add-to-classpath $PLUGIN_PATH/lib/commons-csv-1.3.jar
// add-to-classpath $PLUGIN_PATH/lib/joda-time-2.9.2.jar

invokeOnEDT {
	def pathToTrackingLogFile = "${PathManager.pluginsPath}/activity-tracker/ide-events.csv"
	def trackerLog = new TrackerLog(pathToTrackingLogFile).initWriter(1000L, pluginDisposable)
	def tracker = new ActivityTracker(trackerLog, pluginDisposable, false)
	def propertiesComponent = PropertiesComponent.instance
	def plugin = new ActivityTrackerPlugin(tracker, trackerLog, propertiesComponent).init()
	new PluginUI(plugin, trackerLog, pluginDisposable).init()

	if (!isIdeStartup) show("Reloaded ActivityTracker")
}
