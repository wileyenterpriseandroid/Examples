package com.enterpriseandroidbook.fragmentframework;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PickFragment extends Fragment implements OnItemClickListener {

	// String for logging the class name
	private final String CLASSNAME = getClass().getSimpleName();

	// Turn logging on or off
	private static final boolean L = true;

	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Notification that the fragment is associated with an Activity
		if (L)
			Log.i(CLASSNAME, "onAttach " + activity.getClass().getSimpleName());
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Tell the system we have an options menu
		this.setHasOptionsMenu(true);

		if (null != savedInstanceState)
			restoreState(savedInstanceState);
		
		// Notification that
		if (L)
			Log.i(CLASSNAME, "onCreate");
	}

	// Factor this out of methods that get saved state
	private void restoreState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final ListView list = (ListView) inflater.inflate(
				R.layout.list_frag_list, container, false);
		if (L)
			Log.i(CLASSNAME, "onCreateView");

		attachAdapter(list);
		list.setOnItemClickListener(this);

		return list;
	}

	@Override
    public void onStart() {
		super.onStart();
		if (L)
			Log.i(CLASSNAME, "onStart");
	}

	public void onresume() {
		super.onResume();
		if (L)
			Log.i(CLASSNAME, "onResume");
	}

	@Override
    public void onPause() {
		super.onPause();
		if (L)
			Log.i(CLASSNAME, "onPause");
	}

	@Override
    public void onStop() {
		super.onStop();
		if (L)
			Log.i(CLASSNAME, "onStop");
	}

	@Override
    public void onDestroyView() {
		super.onDestroyView();
		if (L)
			Log.i(CLASSNAME, "onDestroyView");
	}

	@Override
    public void onDestroy() {
		super.onDestroy();
		if (L)
			Log.i(CLASSNAME, "onDestroy");
	}

	@Override
    public void onDetach() {
		super.onDetach();
		if (L)
			Log.i(CLASSNAME, "onDetach");
	}

	// ////////////////////////////////////////////////////////////////////////////
	// Minor lifecycle methods
	// ////////////////////////////////////////////////////////////////////////////

	public void onActivityCreated() {
		// Notification that the containing activiy and its View hierarchy exist
		if (L)
			Log.i(CLASSNAME, "onActivityCreated");
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Overrides of the implementations ComponentCallbacks methods in Fragment
	// /////////////////////////////////////////////////////////////////////////////

	@Override
	public void onConfigurationChanged(Configuration newConfiguration) {
		super.onConfigurationChanged(newConfiguration);

		// This won't happen unless we declare changes we handle in the manifest
		if (L)
			Log.i(CLASSNAME, "onConfigurationChanged");
	}

	@Override
	public void onLowMemory() {
		// No guarantee this is called before or after other callbacks
		if (L)
			Log.i(CLASSNAME, "onLowMemory");
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Menu handling code
	// /////////////////////////////////////////////////////////////////////////////

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.search_menu, menu);
	}

	// ////////////////////////////////////////////////////////////////////////////
	// App-specific code
	// ////////////////////////////////////////////////////////////////////////////

	/**
	 * Attach an adapter that loads the data to the specified list
	 * 
	 * @param list
	 */
	private void attachAdapter(final ListView list) {

		// Make a trivial adapter that loads an array of strings
		ArrayAdapter<String> numbers = new ArrayAdapter<String>(list
				.getContext().getApplicationContext(),
				android.R.layout.simple_list_item_1, new String[] { "one",
						"two", "three", "four", "five", "six" });

		// tell the list to use it
		list.setAdapter(numbers);
		// l.setOnItemClickListener(this);
	}

	// ////////////////////////////////////////////////////////////////////////////
	// Implementation of the OnItemClickListener interface
	// ////////////////////////////////////////////////////////////////////////////

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		// As an example of sending data to our fragments, we will create a
		// bundle
		// with an int and a string, based on which view was clicked
		Bundle data = new Bundle();
		int ordinal = position + 1;
		data.putInt("place", ordinal);
		data.putString("placeName", Integer.toString(ordinal));
		((TabbedActivity) getActivity()).loadTabFragments(data);

	}

}
