package org.random_access.newsreader.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.net.nntp.ArticleInfo;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.apache.commons.net.nntp.NewsgroupInfo;
import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.queries.MessageQueries;
import org.random_access.newsreader.queries.NewsgroupQueries;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.queries.SettingsQueries;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
class NNTPSyncAdapter extends AbstractThreadedSyncAdapter {

    // Global variables
    // Define a variable to contain a content resolver instance
    private static final String TAG = NNTPSyncAdapter.class.getSimpleName();

    private final ContentResolver mContentResolver;
    private final Context context;

    private static final String DATABASE_DATE_PATTERN = "yyyyMMddhhmmss Z";
    private static final String NNTPHEADER_DATE_PATTERN = "EEE, d MMM yyyy HH:mm:ss Z";


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

     // TODO Put the data transfer code here.
    /*
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
                }
                c.moveToNext();
            }
            c.close();
        }
        */
    }


    private void getNewNewsForServer(long id, String server, int port, boolean auth, String user, String password) throws IOException, LoginException{
        NewsgroupQueries newsgroupQueries = new NewsgroupQueries(context);

        NNTPConnector nntpConnector = new NNTPConnector(context);
        NNTPClient client =  nntpConnector.connectToNewsServer(context, server, port, auth, user, password);
        Cursor c = newsgroupQueries.getNewsgroupsOfServer(id);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                getNewNewsForNewsgroup(client, c.getLong(NewsgroupQueries.COL_ID), c.getString(NewsgroupQueries.COL_NAME));
                // TODO use number of messages to keep / number of days to keep messages

            }
        }


    }


    private void  getNewNewsForNewsgroup(NNTPClient client, long groupId, String groupName) throws IOException{



        // get news list from server
        NewGroupsOrNewsQuery query = new NewGroupsOrNewsQuery(new GregorianCalendar(15, 1, 1), true);
        query.addNewsgroup(groupName);
        String[] messages = client.listNewNews(query);
        if (messages == null) {
            messages = applyNextCommand(client, groupName);
        }

        // get news list from database
        MessageQueries messageQueries = new MessageQueries(context);
        Cursor c = messageQueries.getMessagesOfNewsgroup(groupId);
        if (c.moveToFirst()) {

        }
        // todo get date of youngest message





        // compare server & database & load all messages that are not in database

    }


    private String[] applyNextCommand (NNTPClient client, String group) throws  IOException{
        ArrayList<String> articleList = new ArrayList<>();
        client.selectNewsgroup(group);
        ArticleInfo pointer = new ArticleInfo();
        int i = 0;
        while (client.selectNextArticle(pointer) && i < 100){
            // client.selectArticle(pointer.articleNumber, pointer);
            Log.d(TAG, "pointer.articleNumber = " + pointer.articleNumber + ", pointer.articleId = " + pointer.articleId);
            articleList.add(pointer.articleId);
            i++;
        }
        String[] articleArray = new String[articleList.size()];
        return articleList.toArray(articleArray);
    }
}
