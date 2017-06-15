package com.katori770.photowidget;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.AccountPicker;
import com.google.gdata.client.photos.PicasawebService;

public class MainActivity extends AppCompatActivity implements AccountManagerCallback {
    private static final int PICK_ACCOUNT_REQUEST = 9999;

    private PicasawebService picasawebService;
    private AccountManager accountManager;
    private Account selectedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        if (AppManager.getInstance().getUserAccount() == null) {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
            startActivityForResult(intent, PICK_ACCOUNT_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_ACCOUNT_REQUEST:
                if (resultCode == RESULT_OK) {
                    Account[] accountsOnDevice = accountManager.getAccounts();
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                    for (Account account : accountsOnDevice) {
                        if (account.name.equals(accountName)) {
                            selectedAccount = account;
                            break;
                        }
                    }

                    accountManager.getAuthToken(
                            selectedAccount,                     // Account retrieved using getAccountsByType()
                            "lh2",            // Auth scope
                            null,                        // Authenticator-specific options
                            this,                           // Your activity
                            this,          // Callback called when a token is successfully acquired
                            null);    // Callback called if an error occ
                }
                break;
        }
    }

    @Override
    public void run(AccountManagerFuture future) {

    }
}
