package com.enterpriseandroidbook.contactscontractexample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/**
 * @author default-name
 *
 */
/**
 * @author default-name
 *
 */
public class PickFragment extends ListFragment implements
	LoaderManager.LoaderCallbacks<Cursor>, OnMenuItemClickListener {

	// Turn logging on or off
	private static final boolean L = true;
	
	// String for logging the class name
	private final String CLASSNAME = getClass().getSimpleName();
	
	// Tag my loader with this ID
	public static final int LOADER_ID = 42;
	
	// Labels for members saved as state
	private final String STATE_LABEL_NAME = "tablename";
	
	//The current table's class name
	private String tableName;


	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Notification that the fragment is associated with an Activity
		if (L)
			Log.i(CLASSNAME, "onAttach " + activity.getClass().getSimpleName());
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Tell the system we have an options menu
		setHasOptionsMenu(true);
		
		doLoaderCreation(savedInstanceState);
		
		// Notification that
		if (L)
			Log.i(CLASSNAME, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final LinearLayout listLayout = (LinearLayout) inflater.inflate(
				R.layout.list_frag_list, container, false);
		if (L)
			Log.i(CLASSNAME, "onCreateView");

		return listLayout;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(STATE_LABEL_NAME, tableName);
	}

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

	public void onPause() {
		super.onPause();
		if (L)
			Log.i(CLASSNAME, "onPause");
	}

	public void onStop() {
		super.onStop();
		if (L)
			Log.i(CLASSNAME, "onStop");
	}

	public void onDestroyView() {
		super.onDestroyView();
		if (L)
			Log.i(CLASSNAME, "onDestroyView");
	}

	public void onDestroy() {
		super.onDestroy();
		if (L)
			Log.i(CLASSNAME, "onDestroy");
	}

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
	// Overrides of the implementations of ComponentCallbacks methods in Fragment
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
	// ListFragment click handling
	// /////////////////////////////////////////////////////////////////////////////

	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor c = ((CursorAdapter) getListView().getAdapter()).getCursor();
	    String item = buildItemInfo(c, position);
	    String tableInfo = buildDatabaseInfo(c);
	    Bundle data = ((MainActivity) getActivity()).buildDataBundle(item, tableInfo);
	    ((TabbedActivity) getActivity()).loadTabFragments(data);
	}

	// ////////////////////////////////////////////////////////////////////////////
	// Implementation of LoaderCallbacks
	// ////////////////////////////////////////////////////////////////////////////

	// Create the loader, passing in the query
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this.getActivity(), uriForTable(tableName),
	            null, null, null, null);
	}
	
	// Get results
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(cursor);
	}
	
	// Reset
	public void onLoaderReset(Loader<Cursor> loader) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
	    
	}
	
	// ////////////////////////////////////////////////////////////////////////////
	// App-specific code
	// ////////////////////////////////////////////////////////////////////////////
	
	private final static String NL = System.getProperty("line.separator");
	
	
	/**
	 * Called from onCreate. restore state if available
	 * 
	 * @param savedInstanceState
	 */
	private void doLoaderCreation(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // If no saved state, start fresh with the Data table
	    if (null == savedInstanceState) {
	    	openNewTableByName("android.provider.ContactsContract$Data");
	    } else {
	    	// See if we can recreate the query from saved state
	    	tableName = savedInstanceState.getString(STATE_LABEL_NAME);
	    	if (null == tableName) {
	    		doLoaderCreation(null); // Nope
	    	} else {
	    		openNewTableByName(tableName);
	    	}
	    }


	}
	
	/**
	 * Open a new table, based on the name of the class
	 * from the API.
	 * 
	 * @param tableClassName
	 */
	private void openNewTableByName(String tableClassName) {

		Uri table = uriForTable(tableClassName);
		if (null == table) {return;} // Fail silently
		tableName = tableClassName;
		newTableQuery(table, ListColumnMap.get(table));
	}
	
	
	/**
	 * Get the content uri, given the corresponding 
	 * class name
	 * 
	 * @param name The name of the class in ContactsContract
	 * @return The content uri for the corresponding class
	 */
	private Uri uriForTable(String name) {
		Class<?> tableClass;
		Uri table;
		
		// Get the table's class
		try {
			tableClass = Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		// Get the content Uri, which should be static, hence no instance for get
		try {
			table = (Uri)(tableClass.getDeclaredField("CONTENT_URI").get(null));
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (NoSuchFieldException e) {
			return null;
		}
		return table;
	}
	
	private void newTableQuery(Uri table, String column) {
		
		if (null == column || column.isEmpty()) {
			column = BaseColumns._ID;
		}
	    
	    String[] fromColumns = { column };
	    int[] toViews = { android.R.id.text1 };
	
	    // Create an adapter without a cursor
	    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this.getActivity(), 
	            android.R.layout.simple_list_item_1, null,
	            fromColumns, toViews, 0);
	    setListAdapter(adapter);
	
	    // Make a new loader
	    LoaderManager m = getLoaderManager();
	    if (null != m.getLoader(LOADER_ID)) {
	    	m.destroyLoader(LOADER_ID);
	    }
	    m.initLoader(LOADER_ID, null, this);
	}
	
	/**
	 * Extracts, labels, and formats all the information in
	 * all the columns in a row.
	 * 
	 * @param c The cursor
	 * @param position The position in the cursor
	 * @return The formatted data from the row
	 */
	private String buildItemInfo(Cursor c, int position) {
		
		int i;
		int columns = c.getColumnCount();
		String info = "";
		
		c.moveToPosition(position);
		String names[] = c.getColumnNames();

		for (i = 0; i < columns; i++) {
			info += names[i] + ": ";
			try {
				info += c.getString(i);
			} catch (Exception e) {
				// Fail silently
			}
			info += NL;
		}
		
		return info;
	}	
	
	private String buildDatabaseInfo (Cursor c) {
		String info = "";
		
		info += getString(R.string.column_count_label) + c.getColumnCount() + NL;
		info += getString(R.string.row_count_label) + c.getCount() + NL;
				
		return info;
		
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// Methods for transferring data between Fragments
	///////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Build a Bundle that holds the database and item information
	 * 
	 * @param item Information about the selected row
	 * @param dbInfo Information about the database
	 * @return the Bundle containing the above information
	 */
	public Bundle buildDataBundle(String item, String dbInfo) {
		Bundle data = new Bundle();
		
		data.putString(getDataLabel(R.id.item_frag), item);
		data.putString(getDataLabel(R.id.detail_frag), dbInfo);
		return data;
		
	}
	
	public String getDataLabel(int id) {
		Fragment frag = getFragmentManager().findFragmentById(id);
		String label = ((TabbedActivity.SetData)frag).getDataLabel();
		return label;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Menu handling code, including implementation of onMenuItemClickListener
	// /////////////////////////////////////////////////////////////////////////////

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		buildTableMenu(menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		openNewTableByName((String) item.getTitle());
		return true;
	}
	


	// ////////////////////////////////////////////////////////////////////////////
	// App-specific code to create a menu of tables
	// ////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Add a MenuItem to the specified menu for each table in the ContactsContract
	 * class
	 * 
	 * @param menu
	 */
	private void buildTableMenu(Menu menu) {
		Class<?>[] tablesArray = ContactsContract.class.getClasses();
		ArrayList<Class<?>> tablesList = new ArrayList<Class<?>>(Arrays.asList(tablesArray));
		deleteNonTables(tablesList);
		for (Class<?> c : tablesList) {
			menu.add(c.getName()).setOnMenuItemClickListener(this);
			}
	}

	/**
	 * Delete the embedded classes of ContactsContract that are not tables
	 * i.e. not the interfaces, and not the classes that do not implement
	 * BaseColumns.
	 * 
	 * This might miss tables that do not, in fact, implement BaseColumns
	 * but are still tables (but not ones that follow ContactsContract API
	 * conventions)
	 * 
	 * @param tablesList The raw list of embedded classes
	 */
	private void deleteNonTables(ArrayList<Class<?>> tablesList) {
		ListIterator<Class<?>> l = tablesList.listIterator();
		while (l.hasNext()) {
			Class<?> c = l.next();
			
			// Might be belt-and-suspenders
			if (true == c.isInterface()) {
				l.remove();
			}
			else if (false == implementer(c, BaseColumns.class)) {
				l.remove();
			}
		}
	}

	/**
	 * Does the specified class implement the specified interface?
	 * 
	 * @param c The class
	 * @param interf The interface
	 * @return True if the interface is implemented by the class
	 */
	private boolean implementer(Class<?> c, Class<?> interf) {
		for (Class<?> ci : c.getInterfaces()) {
	        if (ci.equals(interf)) {
	            return true;
	        } else {
	        	// Recurse, getInterfaces only gets one level (?!)
	        	if (true == implementer(ci, interf)) {
	        		return true;
	        	}
	        }
	    }
	    return false;
	}
	
}

