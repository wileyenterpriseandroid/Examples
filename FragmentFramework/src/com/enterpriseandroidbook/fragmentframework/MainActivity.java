package com.enterpriseandroidbook.fragmentframework;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

/**
 * @author zigurd
 *
 */
public class MainActivity extends TabbedActivity {
	
	// String for logging the class name
	private final String CLASSNAME = getClass().getSimpleName();
	
	// Turn logging on or off
	private final boolean L = true;
		
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		// To keep this method simple
		doCreate(savedState);
		
		// If we had state to restore, we note that in the log message
		if (L) Log.i(CLASSNAME, "onCreate" + 
				(null == savedState ? " Restored state" : ""));
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// Notification that the activity will be started
		if (L) Log.i(CLASSNAME, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Notification that the activity is starting
		if (L) Log.i(CLASSNAME, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Notification that the activity will interact with the user
		if (L) Log.i(CLASSNAME, "onResume");
	}

	@Override
    protected void onPause() {
		super.onPause();
		// Notification that the activity will stop interacting with the user
		if (L) Log.i(CLASSNAME, "onPause" + (isFinishing() ? " Finishing" : ""));
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Notification that the activity is no longer visible
		if (L) Log.i(CLASSNAME, "onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Notification the activity will be destroyed
		if (L) Log.i(CLASSNAME, "onDestroy"
				// Are we finishing?
				+ (isFinishing() ? " Finishing" : ""));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState(outState);	

		// Called when state should be saved
		if (L) Log.i(CLASSNAME, "onSaveInstanceState");

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		if (null != savedState) restoreState(savedState);
		
		// If we had state to restore, we note that in the log message
		if (L) Log.i(CLASSNAME, "onRestoreInstanceState" + 
				(null == savedState ? " Restored state" : ""));
	}

	///////////////////////////////////////////////////////////////////////////////
	// The minor lifecycle methods - you probably won't need these
	///////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onPostCreate(Bundle savedState) {
		super.onPostCreate(savedState);
		if (null != savedState) restoreState(savedState);
		
		// If we had state to restore, we note that in the log message
		if (L) Log.i(CLASSNAME, "onCreate" + (null == savedState ? " Restored state" : ""));

	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		// Notification that resuming the activity is complete
		if (L) Log.i(CLASSNAME, "onPostResume");
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		// Notification that user navigated away from this activity
		if (L) Log.i(CLASSNAME, "onUserLeaveHint");
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// Overrides of the implementations ComponentCallbacks methods in Activity
	///////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onConfigurationChanged(Configuration newConfiguration) {
		super.onConfigurationChanged(newConfiguration);
		
		// This won't happen unless we declare changes we handle in the manifest
		if (L) Log.i(CLASSNAME, "onConfigurationChanged");
	}
	
	@Override
	public void onLowMemory() {
		// No guarantee this is called before or after other callbacks
		if (L) Log.i(CLASSNAME, "onLowMemory");
	}
    
	///////////////////////////////////////////////////////////////////////////////
	// App-specific code here
	///////////////////////////////////////////////////////////////////////////////

	
	/**
	 * This is where we restore state we previously saved.
	 * @param savedState the Bundle we got from the callback
	 */
	private void restoreState(Bundle savedState) {
		// Add your code to restore state here 
		
	}
	
	/**
	 * Add this activity's state to the bundle and/or commit pending data
	 */
	private void saveState(Bundle state) {
		// Add your code to add state to the bundle here
	}
	
	/**
	 * Perform initializations on creation of this Activity instance
	 * @param savedState
	 */
	private void doCreate(Bundle savedState) {
		setContentView(R.layout.main);
        
		if (null != savedState) restoreState(savedState);
		
        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Initialize the tabs (Fails silently if the tab fragments don't exist)
		int names[] = {R.string.item, R.string.detail };
		int fragments[] = { R.id.content_frag, R.id.detail_frag };
		initializeTabs(0, names, fragments);
	}

}