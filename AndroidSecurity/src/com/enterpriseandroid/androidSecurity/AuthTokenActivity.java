package com.enterpriseandroid.androidSecurity;

import com.enterpriseandroid.androidSecurity.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Use the Android account manger to lists accounts and get
 * an auth token from the migrate account setup for chapter 10.
 */
public class AuthTokenActivity extends BaseActivity{
	/** The tag used to log to adb console. **/
    private static final String TAG = "AuthTokenActivity";
    private static final String ACCOUNT_TYPE="myAccountType";
    
    private AccountManager mAccountManager = null;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_token_activity);
        createGoBackButton();
        final Bundle bundle = savedInstanceState; 
        try {
          	mAccountManager = AccountManager.get(this);
            Account [] accounts =
                    mAccountManager.getAccountsByType("com.enterpriseandroid.syncadaptercontacts.ACCOUNT");
            String accountsList =
                    "Accounts: " + accounts.length + "\n";
            for (Account account : accounts) {
                accountsList += account.toString() + "\n";
            }
            setText(R.id.message, accountsList);
             
        } catch (Exception e) {
        	setText(R.id.message, e.toString());
        }
         
        Button loginBtn = (Button)
                findViewById(R.id.login);
        loginBtn.setOnClickListener( new OnClickListener() {           
            public void onClick(View v) {
                try {
                    Account [] accounts =
                            mAccountManager.getAccounts();
                    if (accounts.length == 0) {
                    	setText(R.id.result, "No Accounts");
                        return;
                    }
                    Account account = accounts[0];
                    mAccountManager.getAuthToken(account,
                    		 "AUTH_UUID", bundle,
                            false, new
                            MyAccountManagerCallback(), null);
                } catch (Exception e) {
                	setText(R.id.result, e.toString());
                }
            }
        });
    }
    
   
    private class MyAccountManagerCallback implements
            AccountManagerCallback<Bundle>
    {
        public void run(AccountManagerFuture<Bundle> result) {
                Bundle bundle;
                try {
                        bundle = result.getResult();
                        Intent intent =
                                (Intent) bundle.get(AccountManager.
                                        KEY_INTENT);
                        if(intent != null) {
                            // asked user for input
                            startActivity(intent);
                        } else {
                        	setText(R.id.result, "auth token: " +
                                    bundle.getString(AccountManager.KEY_AUTHTOKEN));
                        }
                } catch (Exception e) {
                	Log.e(TAG, "accountManagerCallback failed: " + e);
                	setText(R.id.result, e.toString());
                }
        }
    };
     
    public void setText(int id, String msg) {
        TextView tv = (TextView) this.findViewById(id);
        tv.setText(msg);
    }
     
}
