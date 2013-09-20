package com.enterpriseandroidbook.contactscontractexample;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

public class ItemDetailFragment extends DetailMenuActionFragment implements TabListener, TabbedActivity.SetData {
	
	// String for logging the class name
	private final String CLASSNAME = getClass().getSimpleName();
	
	//Turn logging on or off
	private final boolean L = true;
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Notification that the fragment is associated with an Activity
		if (L) Log.i(CLASSNAME, "onAttach " + activity.getClass().getSimpleName());
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(CLASSNAME, "onCreate");
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout content = (FrameLayout) inflater.inflate(R.layout.detail, container, false);
		if (L) Log.i(CLASSNAME, "onCreateView");
		return content;
		
	}

	public void onStart() {
		super.onStart();
		Log.i(CLASSNAME, "onStart");
	}
	
	public void onresume() {
		super.onResume();
		Log.i(CLASSNAME, "onResume");
	}
	
	public void onPause() {
		super.onPause();
		Log.i(CLASSNAME, "onPause");
	}
	
	public void onStop() {
		super.onStop();
		Log.i(CLASSNAME, "onStop");
	}
	
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(CLASSNAME, "onDestroyView");
	}
	
	public void onDestroy() {
		super.onDestroy();
		Log.i(CLASSNAME, "onDestroy");
	}
	
	public void onDetach() {
		super.onDetach();
		Log.i(CLASSNAME, "onDetach");
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Minor lifecycle methods 
	//////////////////////////////////////////////////////////////////////////////
	
	public void onActivityCreated() {
		// Notification that the containing activity and its View hierarchy exist
		Log.i(CLASSNAME, "onActivityCreated");
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// Overrides of the implementations ComponentCallbacks methods in Fragment
	///////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onConfigurationChanged(Configuration newConfiguration) {
		super.onConfigurationChanged(newConfiguration);
		
		// This won't happen unless we declare changes we handle in the manifest
		if (L) Log.i(CLASSNAME, "onConfigurationChanged");
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (L) Log.i(CLASSNAME, "onLowMemory");
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// Implementation of TabListener
	///////////////////////////////////////////////////////////////////////////////

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// Do nothing
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		ft.show(this);
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		ft.hide(this);
		
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// Implementation of SetData
	///////////////////////////////////////////////////////////////////////////////
	
	// Label for sending data to this fragment in the data Bundle
	private static String DATA_LABEL;
	
	@Override
	public void setData(Bundle data) {
		// Display the string spelling out the number
		EditText t = (EditText) getActivity().findViewById(R.id.detail_text);
		String s = data.getString(getDataLabel());
		t.setText(s);		
	}
	
	
	@Override
	public String getDataLabel() {
		if (null == DATA_LABEL) {
			DATA_LABEL = getString(R.string.detail_bundle_label);
		}
		return DATA_LABEL;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}

}
