package org.random_access.newsreader.sync;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.acra.ACRA;
import org.apache.commons.net.nntp.NNTPClient;
import org.random_access.newsreader.nntp.CustomNNTPClient;
import org.random_access.newsreader.queries.DatabaseException;
import org.random_access.newsreader.queries.ServerQueries;
import org.random_access.newsreader.security.CryptUtils;
import org.random_access.newsreader.security.KeyStoreHandlerException;

import java.io.IOException;
import java.security.Key;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
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
     * @param server server name e.g. news.example.com
     * @param port server port [1,..,65535], default: 119
     * @param ssl true, if connection should be opened using SSL, else false
     * @param auth true, if user should be  authenticated, else false
     * @param user username
     * @param password password
     * @return NNTPClient object to communicate with
     * @throws IOException
     */
    public NNTPClient connectToNewsServer(String server, int port, boolean ssl, boolean auth, String user, String password) throws IOException, LoginException, KeyStoreHandlerException {
        if (ssl) {
            return connectUsingSSL(server, port, auth, user, password);
        }
        return connect(server, port, auth, user, password);
    }

    private NNTPClient connect(String server, int port, boolean auth, String user, String password) throws IOException, LoginException, KeyStoreHandlerException {

        NNTPClient nntpClient = new NNTPClient();

        if (port == 0) {
            nntpClient.connect(server);
        } else {
            nntpClient.connect(server, port);
        }

        if (!auth) {
            return nntpClient;
        }

        String plaintextPw = CryptUtils.getInstance().decrypt(password);

        boolean authOk = nntpClient.authenticate(user, plaintextPw);
        if (authOk) {
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }

    class NNTPSSLClient extends NNTPClient {
        private SSLSocket getSSLSocket() {
            return (SSLSocket) _socket_;
        }
    }


    private NNTPClient connectUsingSSL(String server, int port, boolean auth, String user, String password)
            throws IOException, LoginException, KeyStoreHandlerException {

        SocketFactory sf = SSLSocketFactory.getDefault();

        NNTPSSLClient nntpClient = new NNTPSSLClient();

        nntpClient.setSocketFactory(sf);

        if (port == 0) {
            port = 563; // default SSL port for NNTP
        }
        nntpClient.connect(server, port);

        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        SSLSession s = nntpClient.getSSLSocket().getSession();

        if (!hv.verify(server, s)) {
            nntpClient.disconnect();
            throw new SSLHandshakeException("Expected " + server + ", found " + s.getPeerPrincipal());
        }

        if (!auth) {
            return nntpClient;
        }

        String plaintextPw = CryptUtils.getInstance().decrypt(password);

        boolean authOk = nntpClient.authenticate(user, plaintextPw);
        if (authOk) {
            Log.d(TAG, "SSL connection to "  + server + " on port " + port + " established ;)");
            Log.d(TAG, "Cipher suite: " + s.getCipherSuite() + ", Protocol: " + s.getProtocol());
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }


    /**
     * Establish a connection to a given news server with a custom NNTPClient managing different charsets
     * @param serverId database ID of a server entry
     * @return NNTPClient object to communicate with
     * @throws IOException
     */
    public CustomNNTPClient connectToNewsServer(boolean ssl, long serverId, String charset) throws IOException, LoginException, KeyStoreHandlerException {
        ServerQueries sQueries = new ServerQueries(context);
        Cursor c = sQueries.getServerWithId(serverId);
        if (!c.moveToFirst()){
            c.close();
            Log.e(TAG, "Found no server with the given ID in database");
            ACRA.getErrorReporter().handleException(new DatabaseException("Found no server with the given ID in database"));
        }

        CustomNNTPClient nntpClient;
        if (ssl) {
            nntpClient = connectUsingSSL(charset, c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT), c.getInt(ServerQueries.COL_AUTH) == 1,
                    c.getString(ServerQueries.COL_USER), c.getString(ServerQueries.COL_PASSWORD));
        } else {
           nntpClient = connect(charset, c.getString(ServerQueries.COL_NAME), c.getInt(ServerQueries.COL_PORT), c.getInt(ServerQueries.COL_AUTH) == 1,
                   c.getString(ServerQueries.COL_USER), c.getString(ServerQueries.COL_PASSWORD));
        }

        c.close();
        return nntpClient;
    }

    private CustomNNTPClient connect(String charset, String server, int port, boolean auth, String user, String password)
            throws IOException, LoginException, KeyStoreHandlerException {
        CustomNNTPClient nntpClient = new CustomNNTPClient();
        if (charset != null) {
            nntpClient.setCustomEncoding(charset);
        }

        if (port == 0) {
            nntpClient.connect(server);
        } else {
            nntpClient.connect(server, port);
        }

        if (!auth) {
            return nntpClient;
        }

        String plaintextPw = CryptUtils.getInstance().decrypt(password);

        boolean authOk = nntpClient.authenticate(user, plaintextPw);
        if (authOk) {
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }

    private CustomNNTPClient connectUsingSSL(String charset, String server, int port, boolean auth, String user, String password)
            throws IOException, LoginException, KeyStoreHandlerException {

        SocketFactory sf = SSLSocketFactory.getDefault();

        CustomNNTPClient nntpClient = new CustomNNTPClient();
        nntpClient.setSocketFactory(sf);

        if (charset != null) {
            nntpClient.setCustomEncoding(charset);
        }
        if (port == 0) {
            port = 563; // default SSL port for NNTP
        }
        nntpClient.connect(server, port);

        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        SSLSession s = nntpClient.getSSLSocket().getSession();

        if (!hv.verify(server, s)) {
            nntpClient.disconnect();
            throw new SSLHandshakeException("Expected " + server + ", found " + s.getPeerPrincipal());
        }

        if (!auth) {
            return nntpClient;
        }

        String plaintextPw = CryptUtils.getInstance().decrypt(password);

        boolean authOk = nntpClient.authenticate(user, plaintextPw);
        if (authOk) {
            return nntpClient;
        } else {
            throw new LoginException("Login failed");
        }
    }

}
