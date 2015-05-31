package org.random_access.newsreader.sync;

import android.content.Context;

import org.apache.commons.net.nntp.NNTPClient;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class NNTPConnector {

    private final String TAG = NNTPConnector.class.getSimpleName();

    private Context context;

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
}
