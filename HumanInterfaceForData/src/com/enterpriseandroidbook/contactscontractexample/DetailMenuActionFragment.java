package com.enterpriseandroidbook.contactscontractexample;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ShareActionProvider;

public abstract class DetailMenuActionFragment extends Fragment implements OnMenuItemClickListener {

	public DetailMenuActionFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Tell the system we have an options menu
		setHasOptionsMenu(true);
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		inflater.inflate(R.menu.search_menu, menu);
		inflater.inflate(R.menu.share_menu, menu);
		
	    MenuItem item = menu.findItem(R.id.share);
		ShareActionProvider shareAction = 
				(ShareActionProvider) item.getActionProvider();
		shareAction.setShareIntent(new Intent(Intent.ACTION_SEND)
			.setType("text/plain")
			.putExtra(Intent.EXTRA_TEXT, "http://zigurd.com"));
}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.menu.share_menu:
        		ShareActionProvider shareAction = 
				(ShareActionProvider) item.getActionProvider();
		shareAction.setShareIntent(new Intent(Intent.ACTION_SEND)
			.setType("text/plain")
			.putExtra(Intent.EXTRA_TEXT, "http://zigurd.com"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
