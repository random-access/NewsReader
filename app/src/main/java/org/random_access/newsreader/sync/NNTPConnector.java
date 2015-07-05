package org.random_access.newsreader.sync;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.apache.commons.net.nntp.NNTPClient;
import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.queries.ServerQueries;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 25.06.2015 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPConnector {

    private final String TAG = NNTPConnector.class.getSimpleName();

    private final Context context;

    public NNTPConnector(Context context) {
        this.context = context;
    }

    /**
     * Establish a connection to a given news server and return an NNTPCLient object for communication
     * @param context context where the call occurs
     * @param server server name e.g. news.example.com
     * @param port server port [1,..,65535], default: 119
     * @param user username
     * @param password password
     * @return NNTPClient object to communicate with
     * @throws IOException
     */
    public NNTPClient connectToNewsServer(Context context, String server, int port, boolean auth, String user, String password) throws IOException, LoginException {
        NNTPClient nntpClient = new NNTPClient();
        // nntpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
        // TODO handle encrypted connections
        if (port == 0) {
            nntpClient.connect(server);
        } else {
            nntpClient.connect(server, port);
        }
        if (!auth) { // Connection without authentication
            return nntpClient;
        }
        boolean authOk = nntpClient.authenticate(user, password);
        if (authOk) {
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }

    /**
     * Establish a connection to a given news server
     * @param serverId database ID of a server entry
     * @return NNTPClient object to communicate with
     * @throws IOException
     */
   public NNTPClient connectToNewsServer(long serverId, boolean auth) throws IOException, LoginException {
        ServerQueries sQueries = new ServerQueries(context);
        Cursor c = sQueries.getServerWithId(serverId);
        if (!c.moveToFirst()){
            c.close();
            Log.d(TAG, "Found no server with the given ID in database");
            throw new IOException("Found no server with the given ID in database");
        }
        return connectToNewsServer(context, c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT), auth, c.getString(ServerQueries.COL_USER),
                c.getString(ServerQueries.COL_PASSWORD));
    }

    /**
     * Helper method to establish a connection to a given news server with a custom NNTPClient managing different charsets
     * @param serverId database ID of a server entry
     * @return NNTPClient object to communicate with
     * @throws IOException
     */
    public CustomNNTPClient connectToNewsServer(long serverId, String charset) throws IOException, LoginException {
        ServerQueries sQueries = new ServerQueries(context);
        Cursor c = sQueries.getServerWithId(serverId);
        if (!c.moveToFirst()){
            c.close();
            Log.d(TAG, "Found no server with the given ID in database");
            throw new IOException("Found no server with the given ID in database");
        }
        CustomNNTPClient nntpClient = connectToNewsServer(context, charset, c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT), c.getInt(ServerQueries.COL_AUTH) == 1,
                c.getString(ServerQueries.COL_USER), c.getString(ServerQueries.COL_PASSWORD));
        c.close();
        return nntpClient;
    }

    public CustomNNTPClient connectToNewsServer (Context context, String charset, String server, int port, boolean auth, String user, String password)
            throws IOException, LoginException {
        CustomNNTPClient nntpClient = new CustomNNTPClient();
        if (charset != null) {
            nntpClient.setCustomEncoding(charset);
        }
        // TODO handle encrypted connections
        nntpClient.connect(server, port);
        if (!auth) {
            return nntpClient;
        }
        boolean authOk = nntpClient.authenticate(user, password);
        if (authOk) {
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }

}
