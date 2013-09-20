package com.enterpriseandroidbook.contactscontractexample;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public abstract class TabbedActivity extends Activity implements
		ViewPager.OnPageChangeListener {

	private NerfTabListener nerfTabListener = new NerfTabListener();

	/**
	 * Initialize tabs in an activity that uses tabs to switch among fragments
	 * 
	 * @param defaultIndex
	 *            The index of the Fragment shown first
	 * @param nameIDs
	 *            an array of ID for tab names
	 * @param fragmentClasses
	 *            an array of Class objects enabling instantiation of Fragments
	 *            to be tabbed/paged
	 */
	public void initializeTabs(int defaultIndex, int[] nameIDs,
			Class<?>[] fragmentClasses) {

		// Find the pager
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		
		// If there is no pager, there are no tabs
		if (null == pager) {
			return;
		}
		
		// Set the action bar to use tabs
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		/*
		 * Create an adapter that knows our Fragment classes and Activity. This
		 * constructor does most of the heavy lifting because it knows about
		 * both the tabs and the pager.
		 */
		TabbedFragmentPagerAdapter adapter = new TabbedFragmentPagerAdapter(
				pager, nameIDs, fragmentClasses);

		// Tell the adapter it has new items to display
		adapter.notifyDataSetChanged();

		// Select the tab designated as default
		getActionBar().getTabAt(0).select();
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
	 * 
	 * @return The array of Classes to be instantiated and tabbed/paged
	 */
	// public abstract Class<?>[] getTabFragmentClasses();

	/**
	 * An interface to pass data to a Fragment
	 */
	public interface SetData {
		public void setData(Bundle data);

		public String getDataLabel();

		void setDataLabel(String label);
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
		if (null == data)
			return;

		int i;
		ActionBar actionBar = getActionBar();

		for (i = 0; i < n; i++) {
			SetData f = (SetData) actionBar.getTabAt(i).getTag();
			f.setData(data);
		}
	}

	/**
	 * This class is private because we only access it from here and it is
	 * intimately tied to instances of this class. That is, unless it was nested
	 * it would be holding a reference to instances of this component
	 */
	private class TabbedFragmentPagerAdapter extends
			android.support.v13.app.FragmentPagerAdapter {

		private Class<?> fragmentClasses[];
		private int[] nameIDs;

		/**
		 * Create an instance of TabbedFragmentPagerAdapter. This constructor
		 * sets up later instantiation of fragments, and tab creation
		 * 
		 * @param tabbedActivity
		 *            - the activity with tabs and a pager
		 * @param pager
		 *            - the pages that pages the fragments
		 * @param nameIDs
		 *            - the names identifying the fragments
		 * @param fragmentClasses
		 *            - the Fragment subclasses to instantiate
		 */
		TabbedFragmentPagerAdapter(ViewPager pager, int[] nameIDs,
				Class<?>[] fragmentClasses) {
			super(TabbedActivity.this.getFragmentManager());

			/*
			 * The activity implements PageChangeListener, we set it here,
			 * though
			 */

			pager.setOnPageChangeListener((OnPageChangeListener) TabbedActivity.this);

			// Check if there are any Fragments to instantiate
			if (null != fragmentClasses && fragmentClasses.length != 0) {

				/*
				 * Stash a reference to the fragment classes and names for later
				 * use in the callbacks
				 */
				this.fragmentClasses = fragmentClasses;
				this.nameIDs = nameIDs;

				// Get the action bar and remove existing tabs
				ActionBar bar = TabbedActivity.this.getActionBar();
				bar.removeAllTabs();

				// Make new tabs
				int i = 0;
				for (; i < fragmentClasses.length; i++) {

					// Create the tab
					Tab t = bar.newTab().setText(nameIDs[i]);

					// Give it a placebo to chew on
					t.setTabListener(nerfTabListener);

					// Add the tab to the bar
					bar.addTab(t);
				}
			}

			// Set the pager's adapter to this
			pager.setAdapter(this);

			notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int index) {

			ActionBar bar = TabbedActivity.this.getActionBar();
			TabbedPagedFragment f = (TabbedPagedFragment) bar.getTabAt(index)
					.getTag();

			if (null == f) {

				/*
				 * Instantiate the Fragment here. Otherwise it never gets added
				 * to the pager.
				 */
				f = (TabbedPagedFragment) Fragment.instantiate(
						TabbedActivity.this, fragmentClasses[index].getName());

				// Set the data label to the name of the corresponding tab
				f.setDataLabel(TabbedActivity.this.getString(nameIDs[index]));

				// Set the tab's tag, and the TabListener
				bar.getTabAt(index).setTag(f).setTabListener((TabListener) f);
			}

			return f;
		}

		@Override
		public int getCount() {
			return TabbedActivity.this.getActionBar().getTabCount();
		}

	}

	/**
	 * Find a fragment by position
	 * 
	 * @param position
	 *            The position of the fragment in both the ViewPager and as the
	 *            tag of tab
	 * @return
	 */
	public Fragment getFragmentByPosition(int position) {
		return (Fragment) getActionBar().getTabAt(position).getTag();
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Implementation of OnPageChangeListener
	// /////////////////////////////////////////////////////////////////////////////

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// Do nothing

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// Do nothing

	}

	@Override
	public void onPageSelected(int position) {
		getActionBar().setSelectedNavigationItem(position);

	}

	private class NerfTabListener implements TabListener {

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// Do nothing

		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Do nothing

		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// Do nothing

		}

	}

}
