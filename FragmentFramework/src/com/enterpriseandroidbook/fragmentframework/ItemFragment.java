package com.enterpriseandroidbook.fragmentframework;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

public class ItemFragment extends Fragment implements TabListener, TabbedActivity.SetData {
	
	// String for logging the class name
	private final String CLASSNAME = getClass().getSimpleName();
	
	//Turn logging on or off
	private final boolean L = true;
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Notification that the fragment is associated with an Activity
		if (L) Log.i(CLASSNAME, "onAttach " + activity.getClass().getSimpleName());
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Notification that 
		Log.i(CLASSNAME, "onCreate");
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FrameLayout content = (FrameLayout) inflater.inflate(R.layout.content, container, false);
		if (L) Log.i(CLASSNAME, "onCreateView");
		return content;
		
	}

	@Override
    public void onStart() {
		super.onStart();	
		Log.i(CLASSNAME, "onStart");
	}
	
	public void onresume() {
		super.onResume();
		Log.i(CLASSNAME, "onResume");
	}
	
	@Override
    public void onPause() {
		super.onPause();
		Log.i(CLASSNAME, "onPause");
	}
	
	@Override
    public void onStop() {
		super.onStop();
		Log.i(CLASSNAME, "onStop");
	}
	
	@Override
    public void onDestroyView() {
		super.onDestroyView();
		Log.i(CLASSNAME, "onDestroyView");
	}
	
	@Override
    public void onDestroy() {
		super.onDestroy();
		Log.i(CLASSNAME, "onDestroy");
	}
	
	@Override
    public void onDetach() {
		super.onDetach();
		Log.i(CLASSNAME, "onDetach");
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Minor lifecycle methods 
	//////////////////////////////////////////////////////////////////////////////
	
	public void onActivityCreated() {
		// Notification that the containing activiy and its View hierarchy exist
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
		// No guarantee this is called before or after other callbacks
		if (L) Log.i(CLASSNAME, "onLowMemory");
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// Implementation of TabListener
	///////////////////////////////////////////////////////////////////////////////

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void setData(Bundle data) {
		// Display the number
		EditText t = (EditText) getActivity().findViewById(R.id.editText1);
		int i = data.getInt("place");
		t.setText(Integer.toString(i));
	}

}
