package com.enterpriseandroidbook.contactscontractexample;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Intent;
import android.os.Bundle;

public abstract class TabbedActivity extends Activity {

	/**
	 * Initialize tabs in an activity that uses tabs to switch among fragments
	 * 
	 * @param defaultIndex
	 *            The index of the Fragment shown first
	 * @param nameIDs
	 *            an array of ID for tab names
	 * @param fragmentIDs
	 *            an array of IDs of Fragment resources
	 */
	public void initializeTabs(int defaultIndex, int[] nameIDs, int[] fragmentIDs) {

		// How many do we have?
		int n = nameIDs.length;
		int i = 0;

		// Find at least one fragment that should implement TabListener
		TabListener tlFrag = (TabListener) getFragmentManager().findFragmentById(fragmentIDs[i]);

		// Null check - harmless to call if there are no such fragments
		if (null != tlFrag) {

			// Get the action bar and remove existing tabs
			ActionBar bar = getActionBar();
			bar.removeAllTabs();

			// Make new tabs and assign tags and listeners
			for (; i < n; i++) {
				tlFrag = (TabListener) getFragmentManager().findFragmentById(fragmentIDs[i]);
				Tab t = bar.newTab().setText(nameIDs[i]).setTag(tlFrag).setTabListener(tlFrag);
				bar.addTab(t);
			}
			bar.getTabAt(defaultIndex).select();
		}
	}

	/**
	 * If we have tabs and fragments in this activity, pass the bundle data to
	 * the fragments. Otherwise start an activity that should contain the
	 * fragments.
	 * 
	 * @param data
	 */
	public void loadTabFragments(Bundle data) {
		int n = getActionBar().getTabCount();
		if (0 != n) {
			doLoad(n, data);
		} else {
			startActivity(new Intent(this, TabActivity.class).putExtras(data));
		}
	}

	/**
	 * An interface to pass data to a Fragment
	 */
	public interface SetData {
		public void setData(Bundle data);
		public String getDataLabel();
	}

	/**
	 * Iterate over the tabs, get their tags, and use these as Fragment
	 * references to pass the bundle data to the fragments
	 * 
	 * @param n
	 * @param data
	 */
	private void doLoad(int n, Bundle data) {
		
		// Null check - harmless if no data
		if (null == data) return;
		
		int i;
		ActionBar actionBar = getActionBar();
		
		for (i = 0; i < n; i++) {
			SetData f = (SetData) actionBar.getTabAt(i).getTag();
			f.setData(data);
		}
	}


}
