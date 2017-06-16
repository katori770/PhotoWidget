package com.katori770.photowidget.manager;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.AccountPicker;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.GphotoFeed;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.ServiceForbiddenException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GPhotoManager implements AccountManagerCallback {
    private static final String API_PREFIX = "https://picasaweb.google.com/data/feed/api/user/";
    private static final int REQUEST_PICK_ACCOUNT = 9998;
    private static final int REQUEST_AUTHENTICATE = 9997;

    private Context context;
    private PicasawebService picasawebService;
    private AccountManager accountManager;
    private Account userAccount;

    private GPhotoCallback callback;

    public GPhotoManager(Context context) {
        this.context = context;
        accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    public void setCallback(GPhotoCallback callback) {
        this.callback = callback;
    }

    public void initialize(Fragment fragment) {
        if (userAccount == null) {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
            fragment.startActivityForResult(intent, REQUEST_PICK_ACCOUNT);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_ACCOUNT:
                    Account[] accountsOnDevice = accountManager.getAccounts();
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                    for (Account account : accountsOnDevice) {
                        if (account.name.equals(accountName)) {
                            userAccount = account;
                            break;
                        }
                    }
                case REQUEST_AUTHENTICATE:
                    accountManager.getAuthToken(
                            userAccount,                     // Account retrieved using getAccountsByType()
                            "lh2",            // Auth scope
                            null,                        // Authenticator-specific options
                            true,                           // Your activity
                            this,          // Callback called when a token is successfully acquired
                            null);    // Callback called if an error occ

                    break;
            }
        }
    }

    @Override
    public void run(AccountManagerFuture future) {
        if (future.isDone()) {
            try {
                Bundle result = (Bundle) future.getResult();
                Log.i("Test", result.toString());

                if (result.containsKey(AccountManager.KEY_INTENT)) {
                    Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
                    int flags = intent.getFlags();
                    flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
                    intent.setFlags(flags);
                    if (callback != null) {
                        callback.onAuthenticationRequest(intent, REQUEST_AUTHENTICATE);
                    }
                } else if (result.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                    final String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                    picasawebService = new PicasawebService("photowidget-170907");
                    picasawebService.setUserToken(authToken);

                    new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... voids) {
                            List<AlbumEntry> albums = null;
                            try {
                                albums = getAlbums(userAccount.name);
                                Log.d("Album info", "Got " + albums.size() + " albums");
                                for (AlbumEntry myAlbum : albums) {
                                    Log.d("Album info", "Album " + myAlbum.getTitle().getPlainText());
                                }
                                AlbumEntry album = albums.get(0);

                                List<PhotoEntry> photos = getPhotos(album);
                                PhotoEntry photo = photos.get(0);

                                URL photoUrl = new URL(photo.getMediaContents().get(0).getUrl());
                                Bitmap bmp = BitmapFactory.decodeStream(photoUrl.openConnection().getInputStream());
                                return bmp;
                            } catch (ServiceForbiddenException e) {
                                accountManager.invalidateAuthToken("com.google", authToken);
                            } catch (IOException | ServiceException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        protected void onPostExecute(Bitmap result) {
//                            picture.setImageBitmap(result);
                        }
                    }.execute(null, null, null);
                }

            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                e.printStackTrace();
            }
        }
    }

    public <T extends GphotoFeed> T getFeed(String feedHref, Class<T> feedClass) throws IOException, ServiceException {
        Log.d("Feed URL", "Get Feed URL: " + feedHref);
        return picasawebService.getFeed(new URL(feedHref), feedClass);
    }

    public List<AlbumEntry> getAlbums(String userId) throws IOException, ServiceException {
        String albumUrl = API_PREFIX + userId;
        UserFeed userFeed = getFeed(albumUrl, UserFeed.class);

        List<GphotoEntry> entries = userFeed.getEntries();
        List<AlbumEntry> albums = new ArrayList<AlbumEntry>();
        for (GphotoEntry entry : entries) {
            AlbumEntry ae = new AlbumEntry(entry);
            Log.d("Album info", "Album name: " + ae.getName());
            albums.add(ae);
        }

        return albums;
    }

    public List<PhotoEntry> getPhotos(AlbumEntry album) throws IOException, ServiceException {
        AlbumFeed feed = album.getFeed();
        List<PhotoEntry> photos = new ArrayList<PhotoEntry>();
        for (GphotoEntry entry : feed.getEntries()) {
            PhotoEntry pe = new PhotoEntry(entry);
            photos.add(pe);
        }
        Log.d("Album info", "Album " + album.getName() + " has " + photos.size() + " photos");
        return photos;
    }

    public interface GPhotoCallback {
        void onAuthenticationRequest(Intent intent, int requestCode);
    }
}
