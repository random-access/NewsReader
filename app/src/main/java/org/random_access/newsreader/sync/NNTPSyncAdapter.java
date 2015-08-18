package org.random_access.newsreader.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.net.nntp.ArticleInfo;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.random_access.newsreader.NetworkStateHelper;
import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.nntp.NNTPDateFormatter;
import org.random_access.newsreader.nntp.NNTPMessageBody;
import org.random_access.newsreader.nntp.NNTPMessageHeader;
import org.random_access.newsreader.queries.MessageQueries;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.queries.SettingsQueries;

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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wifiOnly = sharedPreferences.getBoolean("pref_wlan_only", false);
        Log.d(TAG, "Sync only via WIFI? " + wifiOnly);
        boolean hasWifiConnection = NetworkStateHelper.hasWifiConnection(context);
        Log.d(TAG, "Has WIFI connection?" + hasWifiConnection);
        if (!wifiOnly || hasWifiConnection) {
            Log.d(TAG, "*************** SYNCING: " + ++syncNumber + " *****************");
            ServerQueries serverQueries = new ServerQueries(context);
            Cursor c = serverQueries.getAllServers();
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    try {
                        getNewNewsForServer(c.getLong(ServerQueries.COL_ID), c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT),
                                c.getInt(ServerQueries.COL_AUTH) == 1, c.getString(ServerQueries.COL_USER),
                                c.getString(ServerQueries.COL_PASSWORD));
                    } catch (IOException | LoginException e) {
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
        }
    }

    private void getNewNewsForServer(long serverId, String server, int port, boolean auth, String user, String password) throws IOException, LoginException {
        NewsgroupQueries newsgroupQueries = new NewsgroupQueries(context);

        NNTPConnector nntpConnector = new NNTPConnector(context);
        NNTPClient client =  nntpConnector.connectToNewsServer(context, server, port, auth, user, password);
        Cursor c = newsgroupQueries.getNewsgroupsOfServer(serverId);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Log.d(TAG, "Starting sync for Newsgroup " + c.getString(NewsgroupQueries.COL_NAME) + "( id " + c.getLong(NewsgroupQueries.COL_ID) + ")");
                getNewNewsForNewsgroup(serverId, client, c.getLong(NewsgroupQueries.COL_ID), c.getString(NewsgroupQueries.COL_NAME), c.getInt(NewsgroupQueries.COL_MSG_LOAD_INTERVAL));
                // TODO cleanup old news -> use number of messages to keep / number of days to keep messages
                Log.d(TAG, "Finished sync for Newsgroup " + c.getString(NewsgroupQueries.COL_NAME) + "( id " + c.getLong(NewsgroupQueries.COL_ID) + ")");
                c.moveToNext();
            }
        }
        c.close();
    }


    private void  getNewNewsForNewsgroup(long serverId, NNTPClient client, long groupId, String groupName, int syncTime) throws IOException, LoginException{
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
            fetchMessage(serverId, groupId, s);
        }

        // Store date of last message that we fetched in newsgroup table and reset values.
        if (currentNewsgroupId != -1 && currentMessageDate != -1) {
            newsgroupQueries.setLastSyncDate(groupId, currentMessageDate);
        }

        currentNewsgroupId = -1;
        currentMessageDate = -1;
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

    private void fetchMessage(long serverId, long groupId, String articleId) throws IOException, LoginException{

        if (new MessageQueries(context).isMessageInDatabase(articleId)) {
            return;
        }

        NNTPMessageHeader headerData = null;
        long msgDate = -1;
        String messageBody = null;

        try {
            // fetch header
            CustomNNTPClient client = new NNTPConnector(context).connectToNewsServer(serverId, null);
            BufferedReader reader = new BufferedReader(client.retrieveArticleHeader(articleId));
            headerData = new NNTPMessageHeader();
            headerData.parseHeaderData(reader, articleId, context);
            String charset = headerData.getCharset();
            Log.d(TAG, charset);
            String transferEncoding = headerData.getTransferEncoding();
            msgDate = new NNTPDateFormatter().getDateInMillis(headerData.getDate());
            client.disconnect();

            // fetch body
            client = new NNTPConnector(context).connectToNewsServer(serverId, charset);
            reader = new BufferedReader(client.retrieveArticleBody(articleId));
            messageBody = new NNTPMessageBody().parseBodyData(reader, charset, transferEncoding);
            client.disconnect();

        } catch (IOException e) {
            Log.e(TAG, "Error during fetchMessage - connection got interrupted");
            throw  new IOException();
        } catch(LoginException e) {
            Log.e(TAG, "Error during fetchMessage - login failed");
            throw new LoginException();
        }
        if (msgDate != -1 && messageBody != null) {
            // save message to database
            MessageQueries messageQueries = new MessageQueries(context);
            messageQueries.addMessage(articleId, headerData.getEmail(), headerData.getFullName(), headerData.getSubject(), headerData.getCharset(),
                    msgDate, 1, groupId, headerData.getHeaderSource(), messageBody, headerData.getParentMsg(), headerData.getRootMsg(), headerData.getLevel(),
                    headerData.getReferences());
            currentMessageDate = msgDate;
            Log.d(TAG, "Added message " + articleId + "; messageDate " + NNTPDateFormatter.getPrettyDateString(msgDate, context));
        }
    }
}
