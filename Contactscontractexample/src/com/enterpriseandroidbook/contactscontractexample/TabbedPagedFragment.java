package com.enterpriseandroidbook.contactscontractexample;

import com.enterpriseandroidbook.contactscontractexample.TabbedActivity.SetData;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class TabbedPagedFragment extends Fragment implements
		TabListener, SetData {

	private boolean fragmentLayoutInflated = false;
	private boolean attached = false;

	// Label for sending data to this fragment in the data Bundle
	private String dataLabel = null;
	private Bundle stashedData = null;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		attached = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		fragmentLayoutInflated = true;
		return null;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// Do nothing

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		if (true == attached) {

			// The ViewPager is used to show the specified Fragment
			ViewPager pager = (ViewPager) getActivity()
					.findViewById(R.id.pager);

			// Check that we need to change current fragments
			if (pager.getCurrentItem() != tab.getPosition()) {
				pager.setCurrentItem(tab.getPosition());
			}
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// Do nothing

	}

	// /////////////////////////////////////////////////////////////////////////////
	// Implementation of SetData
	// /////////////////////////////////////////////////////////////////////////////

	@Override
	public String getDataLabel() {
		return dataLabel;
	}

	@Override
	public void setDataLabel(String label) {
		dataLabel = label;
	}

	/**
	 * Implementation helpers for setData
	 */
	public boolean readyToSet(Bundle data) {
		if (true == fragmentLayoutInflated) {
			return true;
		} else {
			stashedData = data;
		}
		return false;
	}

	public Bundle getStashedData() {
		return stashedData;
	}

}
