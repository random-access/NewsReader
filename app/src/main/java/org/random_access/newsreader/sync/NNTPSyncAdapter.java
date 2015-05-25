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
 * Project: FlashCards Manager for Android
 * Date: 18.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class NNTPSyncAdapter extends AbstractThreadedSyncAdapter {

    // Global variables
    // Define a variable to contain a content resolver instance
    private static final String TAG = NNTPSyncAdapter.class.getSimpleName();

    private ContentResolver mContentResolver;
    private Context context;


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
        NNTPClient nntpClient = connectToNewsServer(context, serverId);
        ArrayList<String> youngestMessageIds = getYoungestMessageIds(context, newsgroupId);
        String dateOfYoungestMessages = getDateOfYoungestMessages(context, newsgroupId);
        int numberOfMessagesToKeep = getNumberOfMessagesToKeep(context, serverId);
        int numberOfDaysToKeepMessages = getNumberOfDaysForKeepingMessages(context, serverId);
        // TODO find out how to sync this
        NewGroupsOrNewsQuery query = new NewGroupsOrNewsQuery(parseDateStringToGregorianCalendar(dateOfYoungestMessages), true);
        query.addNewsgroup(newsgroupName);
        String[] newNews = nntpClient.listNewNews(query);
        Log.d("Test", "New news for " + newsgroupName + ": " + newNews.length);
        Log.d("Content", newsListTostring(newNews));
        // TODO save message headers to database
    }

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
     * Helper method for getting the number of days to keep message headers in memory
     * @param context the Sync context
     * @param serverId database _ID field identifying a Server entry
     * @return int - number of days to keep messages
     * @throws IOException
     */
    private int getNumberOfDaysForKeepingMessages(Context context, long serverId) throws IOException{
        SettingsQueries sQueries = new SettingsQueries(context);
        Cursor c =  sQueries.getSettingsForServer(serverId);
        if (!c.moveToFirst()) {
            c.close();
            throw new IOException("No settings for the given newsgroup!");
        } else {
            int i =  c.getInt(SettingsQueries.COL_MSG_KEEP_DAYS);
            c.close();
            return i;
        }
    }

    /**
     * Helper method for getting the nummber of messages to keep in memory
     * @param context the Sync context
     * @param serverId database _ID field identifying a Server entry
     * @return int - number of messages to keep
     * @throws IOException
     */
    private int getNumberOfMessagesToKeep(Context context, long serverId) throws IOException{
        SettingsQueries sQueries = new SettingsQueries(context);
        Cursor c = sQueries.getSettingsForServer(serverId);
        if (c.getCount() > 0 ) {
            c.moveToFirst();
            int i = c.getInt(SettingsQueries.COL_MSG_KEEP_NO);
            c.close();
            return i;
        } else {
            c.close();
            throw new IOException("No settings for server with ID " + serverId + " found!");
        }
    }

    /**
     * Helper method to get the name of a newsgroup for a given ID
     * @param context the Sync context
     * @param newsGroupId database _ID field identifying a Newsgroup entry
     * @return String containing the name of the given newsgroup, e.g. formatted like this: "section1.section2.*.sectionl"
     * @throws IOException if there is no newsgroup matching the ID
     */
    private String getNewsgroupName(Context context, long newsGroupId) throws IOException {
        NewsgroupQueries nQueries = new NewsgroupQueries(context);
        Cursor c = nQueries.getNewsgroupForId(newsGroupId);
        if (!c.moveToFirst()) {
            throw new IOException("No newsgroup with the given ID found");
        }
        String newsgroupName = c.getString(NewsgroupQueries.COL_NAME);
        c.close();
        return newsgroupName;
    }

    /**
     * Helper method to get all message ID's with the youngest date in the database
     * @param context the Sync context
     * @param newsGroupId database _ID field identifying a Newsgroup entry
     * @return ArrayList<String> containing 0 ... n message ID's
     */
    private ArrayList<String> getYoungestMessageIds(Context context, long newsGroupId) {
        MessageQueries mQueries = new MessageQueries(context);
        ArrayList<String> youngestMessageIds = new ArrayList<>();
        Cursor cursor = mQueries.getCursorToYoungestMessage(newsGroupId);
        if ( cursor.moveToFirst()) {
            String date = cursor.getString(MessageQueries.COL_DATE);
            String nextDate = date;
            while (nextDate.equals(date)) {
                youngestMessageIds.add(cursor.getString(MessageQueries.COL_MSG_ID));
                cursor.moveToNext();
                nextDate = cursor.getString(MessageQueries.COL_DATE);
            }
        }
        cursor.close();
        return youngestMessageIds;
    }

    /**
     * Helper method to get the date of the youngest message in the database or null if there is no message yet
     * @param context the Sync context
     * @param newsGroupId database _ID field identifying a Newsgroup entry
     * @return String date in the format [yymmddhhmmss]
     */
    private String getDateOfYoungestMessages(Context context, long newsGroupId) {
        MessageQueries mQueries = new MessageQueries(context);
        ArrayList<String> youngestMessageIds = new ArrayList<>();
        Cursor cursor = mQueries.getCursorToYoungestMessage(newsGroupId);
        if (cursor.moveToFirst()) {
            String s = cursor.getString(MessageQueries.COL_DATE);
            cursor.close();
            return s;
        } else {
            cursor.close();
            return "150101000000";
        }
    }




    /**
     * Helper method to establish a connection to a given news server
     * @param context context of the sync operation
     * @param serverId database ID of a server entry
     * @returna NNTPClient object to communicate with
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

    private GregorianCalendar parseDateStringToGregorianCalendar(String dateString) throws ParseException {
        Log.d(TAG, "Date to parse: " + dateString);
        DateFormat df = new SimpleDateFormat("yymmddhhmmss");
        Date date = df.parse(dateString);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

}
