package org.random_access.newsreader.sync;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.acra.ACRA;
import org.apache.commons.net.nntp.ArticleInfo;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.random_access.newsreader.NetworkStateHelper;
import org.random_access.newsreader.R;
import org.random_access.newsreader.SettingsActivity;
import org.random_access.newsreader.ShowServerActivity;
import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.nntp.NNTPParsingException;
import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.nntp.NNTPMessageBody;
import org.random_access.newsreader.nntp.NNTPMessageHeader;
import org.random_access.newsreader.queries.MessageQueries;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.receivers.NotificationDismissReceiver;
import org.random_access.newsreader.security.KeyStoreHandlerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPSyncAdapter extends AbstractThreadedSyncAdapter {

    // Global variables
    // Define a variable to contain a content resolver instance
    private static final String TAG = NNTPSyncAdapter.class.getSimpleName();

    public static final String SYNC_REQUEST_TAG = "Sync-Request";
    public static final String SYNC_REQUEST_ORIGIN = "Sync-Origin";

    private static int syncNumber = 0;

    private final ContentResolver mContentResolver;
    private final Context context;

    private long currentNewsgroupId = -1;
    private long currentMessageDate = -1;

    private static final int NOTIFICATION_ID = 0;
    // Notification Sound and Vibration on Arrival
    private Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
   //  Uri.parse("android.resource://org.random_access.newsreader/"
                  //   + R.raw.mysound;
    private long[] vibratePattern = { 0, 200, 200, 300 };

    /**
     * Set up the sync adapter
     */
    public NNTPSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        this.context = context;
        mContentResolver = context.getContentResolver();
    }
    /**
     * Set up the sync adapter. This form of the
     * constructor malongains compatibility with Android 3.0
     * and later platform versions
     */
    public NNTPSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        this.context = context;
        mContentResolver = context.getContentResolver();
    }

    /*
    * Specify the code you want to run in the sync adapter. The entire
    * sync adapter runs in a background thread, so you don't have to set
    * up your own background processing.
    */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);

                // PreferenceManager.getDefaultSharedPreferences(context);
        boolean wifiOnly = sharedPreferences.getBoolean("pref_wlan_only", false);
        boolean notify = sharedPreferences.getBoolean("pref_notify_on_sync", true);
        Log.d(TAG, "Sync only via WIFI? " + wifiOnly + ", Notify user? " + notify);
        boolean hasWifiConnection = NetworkStateHelper.hasWifiConnection(context);
        Log.d(TAG, "Has WIFI connection?" + hasWifiConnection);
        if (!wifiOnly || hasWifiConnection) {
            Log.d(TAG, "*************** SYNCING: " + ++syncNumber + " *****************");
            int freshMessages = 0;
            ServerQueries serverQueries = new ServerQueries(context);
            Cursor c = serverQueries.getAllServers();
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    try {
                        getNewNewsForServer(c.getLong(ServerQueries.COL_ID), c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT),
                                c.getInt(ServerQueries.COL_ENCRYPTION) == 1, c.getInt(ServerQueries.COL_AUTH) == 1, c.getString(ServerQueries.COL_USER),
                                c.getString(ServerQueries.COL_PASSWORD));
                        freshMessages += new MessageQueries(context).getFreshMessagesOnServerCount(c.getLong(ServerQueries.COL_ID));
                    } catch (IOException | LoginException | KeyStoreHandlerException e) {
                        e.printStackTrace();
                    } finally {
                        // if we get interrupted during syncing a newsgroup, store date of last message that was fetched in order
                        // to start the sync next time at the right time
                        if (currentNewsgroupId != -1 && currentMessageDate != -1) {
                            Log.d(TAG, "-----> Sync interrupted! Last sync date in group " + currentNewsgroupId + " is " + currentMessageDate);
                            NewsgroupQueries newsgroupQueries = new NewsgroupQueries(context);
                            newsgroupQueries.setLastSyncDate(currentNewsgroupId, currentMessageDate);
                            currentMessageDate = -1;
                            currentNewsgroupId = -1;
                        }
                    }
                    c.moveToNext();
                }
                Log.d(TAG, "************ FINISHED SYNC: " + syncNumber + "*********************");
            }
            c.close();
            if (notify && freshMessages > 0) {
                setNotification(freshMessages);
            }
        }
    }

    private void getNewNewsForServer(long serverId, String server, int port, boolean ssl, boolean auth, String user, String password) throws IOException, LoginException, KeyStoreHandlerException {
        NewsgroupQueries newsgroupQueries = new NewsgroupQueries(context);

        NNTPConnector nntpConnector = new NNTPConnector(context);
        NNTPClient client =  nntpConnector.connectToNewsServer(server, port, ssl, auth, user, password);
        Cursor c = newsgroupQueries.getNewsgroupsOfServer(serverId);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Log.d(TAG, "Starting sync for Newsgroup " + c.getString(NewsgroupQueries.COL_NAME) + "( id " + c.getLong(NewsgroupQueries.COL_ID) + ")");
                getNewNewsForNewsgroup(serverId, ssl, client, c.getLong(NewsgroupQueries.COL_ID), c.getString(NewsgroupQueries.COL_NAME), c.getInt(NewsgroupQueries.COL_MSG_LOAD_INTERVAL));
                cleanupOldNewsFromNewsgroup(c.getLong(NewsgroupQueries.COL_ID), c.getInt(NewsgroupQueries.COL_MSG_KEEP_INTERVAL));
                Log.d(TAG, "Finished sync for Newsgroup " + c.getString(NewsgroupQueries.COL_NAME) + "( id " + c.getLong(NewsgroupQueries.COL_ID) + ")");
                c.moveToNext();
            }
        }
        c.close();
    }


    private void  getNewNewsForNewsgroup(long serverId, boolean ssl, NNTPClient client, long groupId, String groupName, int syncTime) throws IOException, LoginException{
        currentNewsgroupId = groupId;

        // Create a GregorianCalendar instance with date of last sync.
        NewsgroupQueries newsgroupQueries = new NewsgroupQueries(context);
        long lastSyncDate = newsgroupQueries.getLastSyncDate(groupId);
        GregorianCalendar calendar = new GregorianCalendar();
        if (lastSyncDate != -1) {
            calendar.setTimeInMillis(lastSyncDate); // TODO compare mails that arrived in this second with database entries to not lose messages.
            Log.d(TAG, "Last synced: " + NNTPDateFormatter.getPrettyDateString(lastSyncDate, context));
        } else {
            // Set sync interval; TODO get sync interval from settings
            calendar.setTimeInMillis(System.currentTimeMillis() -  TimeUnit.MILLISECONDS.convert(syncTime, TimeUnit.DAYS));
            Log.d(TAG, "Complete sync: " + NNTPDateFormatter.getPrettyDateString(calendar.getTimeInMillis(), context));
        }

        // get list of message id's from server
        NewGroupsOrNewsQuery query = new NewGroupsOrNewsQuery(calendar, false);
        query.addNewsgroup(groupName);
        String[] messages = client.listNewNews(query);
        if (messages == null) {
            messages = applyNextCommand(client, groupName);
        }

        // Get messages and add them to database.
        for (String s : messages) {
            fetchMessage(serverId, ssl, groupId, s);
        }

        // Store date of last message that we fetched in newsgroup table and reset values.
        if (currentNewsgroupId != -1 && currentMessageDate != -1) {
            newsgroupQueries.setLastSyncDate(groupId, currentMessageDate);
        }

        currentNewsgroupId = -1;
        currentMessageDate = -1;
    }

    // delete all news older than the current date minus the timespan
    // to keep messages
    private void cleanupOldNewsFromNewsgroup(long groupId, long keepInterval) {
        if (keepInterval != -1) {
            long keepTime = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(keepInterval, TimeUnit.DAYS);
            Log.d(TAG, "Delete messages older than: " + NNTPDateFormatter.getPrettyDateString(keepTime, context));
            new MessageQueries(context).deleteOldMessages(groupId, keepTime);
        }
    }

    //Fallback method if news server doesnt' support listNewNews -> this is much slower
    // but at least we get the messages
    private String[] applyNextCommand (NNTPClient client, String group) throws  IOException{
        ArrayList<String> articleList = new ArrayList<>();
        client.selectNewsgroup(group);
        ArticleInfo pointer = new ArticleInfo();
        int i = 0;
        while (client.selectNextArticle(pointer) && i < 100){ // TODO while date > sync start date
            // client.selectArticle(pointer.articleNumber, pointer);
            Log.d(TAG, "pointer.articleNumber = " + pointer.articleNumber + ", pointer.articleId = " + pointer.articleId);
            articleList.add(pointer.articleId);
            i++;
        }
        String[] articleArray = new String[articleList.size()];
        return articleList.toArray(articleArray);
    }

    private void fetchMessage(long serverId, boolean ssl, long groupId, String articleId) throws IOException, LoginException {

        if (new MessageQueries(context).isMessageInDatabase(articleId, groupId)) {
            return;
        }

        NNTPMessageHeader headerData = null;
        long msgDate = 0;
        String messageBody = null;

        try {
            // fetch header
            CustomNNTPClient client = new NNTPConnector(context).connectToNewsServer(ssl, serverId, null);
            BufferedReader reader = new BufferedReader(client.retrieveArticleHeader(articleId));
            headerData = new NNTPMessageHeader();
            headerData.parseHeaderData(reader, articleId, groupId, context);
            String charset = headerData.getCharset();
            Log.d(TAG, charset);
            String transferEncoding = headerData.getTransferEncoding();
            Log.d(TAG, transferEncoding);
            msgDate = new NNTPDateFormatter().getDateInMillis(headerData.getDate());
            client.disconnect();

            // fetch body
            client = new NNTPConnector(context).connectToNewsServer(ssl, serverId, charset);
            reader = new BufferedReader(client.retrieveArticleBody(articleId));
            messageBody = new NNTPMessageBody().parseBodyData(reader, charset, transferEncoding);
            client.disconnect();

        } catch (IOException e) {
            Log.e(TAG, "Error during fetchMessage - connection got interrupted");
            throw  new IOException();
        } catch(LoginException e) {
            Log.e(TAG, "Error during fetchMessage - login failed");
            throw new LoginException();
        } catch(NNTPParsingException | KeyStoreHandlerException e) {
            ACRA.getErrorReporter().handleException(e);
        }
        if (msgDate != -1 && messageBody != null) {
            // save message to database
            MessageQueries messageQueries = new MessageQueries(context);
            messageQueries.addMessage(articleId, headerData.getEmail(), headerData.getFullName(), headerData.getSubject(), headerData.getCharset(),
                    msgDate, 1, groupId, headerData.getHeaderSource(), messageBody, headerData.getParentMsg(), headerData.getRootMsg(), headerData.getLevel(),
                    headerData.getReferences(), 1);
            currentMessageDate = msgDate;
            Log.d(TAG, "Added message " + articleId + "; messageDate " + NNTPDateFormatter.getPrettyDateString(msgDate, context));
        }
    }

    private void setNotification (int freshMessages) {
        Log.d(TAG, "***** notification coming... *****");

        // Target activity for onClick = ShowServerActivity
        Intent clickIntent = new Intent(context, ShowServerActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ShowServerActivity.class);
        stackBuilder.addNextIntent(clickIntent);
        PendingIntent pendingClickIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Target for dismissing notification
        Intent deleteIntent = new Intent(context, NotificationDismissReceiver.class);
        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

        // add intents to builder
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setColor(ContextCompat.getColor(context, R.color.blue))
                       // .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(context.getResources().getQuantityString(R.plurals.new_news, freshMessages, freshMessages))
                        .setSound(soundURI)
                        .setVibrate(vibratePattern)
                        .setAutoCancel(true)
                        .setContentIntent(pendingClickIntent)
                .setDeleteIntent(pendingDeleteIntent);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_newsreader_inv);
        } else {
            builder.setSmallIcon(R.drawable.ic_newsreader);
        }

        // dispatch notification, allowing future updates of an existing notification
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}
