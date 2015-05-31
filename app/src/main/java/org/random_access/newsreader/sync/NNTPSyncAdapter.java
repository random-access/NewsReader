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

import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.apache.commons.net.nntp.NewsgroupInfo;
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
public class NNTPSyncAdapter extends AbstractThreadedSyncAdapter {

    // Global variables
    // Define a variable to contain a content resolver instance
    private static final String TAG = NNTPSyncAdapter.class.getSimpleName();

    private ContentResolver mContentResolver;
    private Context context;

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
    /*
     * TODO Put the data transfer code here.

        Log.d(TAG, "*** Performing sync ... to be implemented... ***");
        ServerQueries serverQueries = new ServerQueries(context);
        Cursor serverCursor = serverQueries.getAllServers();
        if (serverCursor.getCount() > 0) {
            serverCursor.moveToFirst();
            while (!serverCursor.isAfterLast()) {
                long serverId = serverCursor.getLong(ServerQueries.COL_ID);
                NewsgroupQueries newsgroupQueries = new NewsgroupQueries(context);
                Cursor newsgroupCursor = newsgroupQueries.getNewsgroupsOfServer(serverId);
                if (newsgroupCursor.getCount() > 0 ) {
                    newsgroupCursor.moveToFirst();
                    while (!newsgroupCursor.isAfterLast()) {
                        String newsgroupName = newsgroupCursor.getString(NewsgroupQueries.COL_NAME);
                        long newsgroupId = newsgroupCursor.getInt(NewsgroupQueries.COL_ID);
                        try {
                            syncMessageHeaders(context, serverId, newsgroupId, newsgroupName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (LoginException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        newsgroupCursor.moveToNext();
                    }
                    newsgroupCursor.close();
                }
                serverCursor.moveToNext();
            }
            serverCursor.close();
        }
        */
    }


    /**
     * Get new message headers from a given newsgroup ID
     * Get last [count] of messages is not yet implemented!!!
     * @param newsgroupId database _ID field identifying a NewsGroup entry
     */
    private void syncMessageHeaders(Context context, long serverId, long newsgroupId, String newsgroupName) throws IOException, LoginException, ParseException{
        // get infos from database
        ArrayList<String> youngestMessageIds = new MessageQueries(context).getListOfYoungestMessagesInNewsgroup(newsgroupId);
        int numberOfMessagesToKeep = new SettingsQueries(context).getNumberOfMessagesToKeep(context, serverId);
        int numberOfDaysToKeepMessages = new SettingsQueries(context).getNumberOfDaysForKeepingMessages(context, serverId);

        // connect to server to get the message ID's
        NNTPClient nntpClient = connectToNewsServer(context, serverId);
        NewGroupsOrNewsQuery query = new NewGroupsOrNewsQuery(parseDateStringToGregorianCalendar(youngestMessageIds.get(0), DATABASE_DATE_PATTERN), true);
        query.addNewsgroup(newsgroupName);
        String[] newNews = nntpClient.listNewNews(query);

        // TODO save message headers to database
    }

    /**********************************************************************************************************************/
    private String newsListTostring (String[] list) {
        StringBuilder sb = new StringBuilder("***********NEWS****************");
        for (String s : list) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }


    /**
     * Method for getting all available Newsgroupnames from a given server ID
     * @param context the Sync context
     * @param serverId database _ID field identifying a Server entry
     * @return string array of all newsgroup names
     * @throws IOException if
     */
    private String[] getAvailableNewsgroups(Context context, long serverId) throws IOException, LoginException {
        NNTPClient nntpClient = connectToNewsServer(context,serverId);
        NewsgroupInfo[] infos = nntpClient.listNewsgroups();
        return getNewsgroupNames(infos);
    }

    /*************************************************************************************************************************/



    /**
     * Helper method to establish a connection to a given news server
     * @param context context of the sync operation
     * @param serverId database ID of a server entry
     * @return NNTPClient object to communicate with
     * @throws IOException
     */
    private NNTPClient connectToNewsServer(Context context, long serverId) throws IOException, LoginException{
        ServerQueries sQueries = new ServerQueries(context);
        Cursor c = sQueries.getServerWithId(serverId);
        if (!c.moveToFirst()){
            c.close();
            Log.d(TAG, "Found no server with the given ID in database");
            throw new IOException("Found no server with the given ID in database");
        }
        NNTPClient nntpClient = new NNTPClient();
        // nntpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
        // TODO handle encrypted connections
        nntpClient.connect(c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT));
        boolean authOk = nntpClient.authenticate(c.getString(ServerQueries.COL_USER), c.getString(ServerQueries.COL_PASSWORD));
        c.close();
        if (authOk) {
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }

    /**
     * Helper method to convert NewsgroupInfo objects to Strings containing the newsgroup name
     * @param infos a NewsgroupInfo[] object (e.g. obtained by listNewsgroups()
     * @return a String[] object with newsgroup names
     */
    private String[] getNewsgroupNames(NewsgroupInfo[] infos) {
        String [] groupNames = new String[infos.length];
        for (int i = 0; i < infos.length; i++) {
            groupNames[i] = infos[i].getNewsgroup();
        }
        return groupNames;
    }


    // pattern database: "yyyyMMddhhmmss Z"
    // ----> eg 20150502181729 +0200
    // pattern message header: "EEE, d MMM yyyy HH:mm:ss Z"
    // ----> eg Sat, 02 May 2015 18:17:29 +0200
    private GregorianCalendar parseDateStringToGregorianCalendar(String dateString, String pattern) throws ParseException {
        Log.d(TAG, "Date to parse: " + dateString);
        DateFormat df = new SimpleDateFormat(pattern);
        Date date = df.parse(dateString);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

}
