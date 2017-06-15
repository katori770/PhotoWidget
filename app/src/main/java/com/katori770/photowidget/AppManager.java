package com.katori770.photowidget;


import android.accounts.Account;
import android.content.Context;

public class AppManager {
    private static AppManager instance;

    private Context context;
    private Account userAccount;

    public AppManager(Context context) {
        this.context = context;
    }

    public static AppManager getInstance() {
        return instance;
    }

    public static synchronized void create(Context context) {
        instance = new AppManager(context);
    }

    public Account getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(Account userAccount) {
        this.userAccount = userAccount;
    }
}
