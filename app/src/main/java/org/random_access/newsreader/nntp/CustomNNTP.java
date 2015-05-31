package org.random_access.newsreader.nntp;

import org.apache.commons.net.io.CRLFLineReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Project: FlashCards Manager for Android
 * Date: 25.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 * *
 * **** This class is a slightly modified version of the Apache Commons Net's NNTPClient class ****
 *            The license can be found here: http://www.apache.org/licenses/LICENSE-2.0
 */

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ProtocolCommandSupport;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.nntp.NNTPCommand;
import org.apache.commons.net.nntp.NNTPConnectionClosedException;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 *
 * **** This class is a slightly modified version of the Apache Commons Net's NNTPClient class ****
 *              The license can be found here: http://www.apache.org/licenses/LICENSE-2.0
 */
public class CustomNNTP extends SocketClient {
    public static final int DEFAULT_PORT = 119;
    private static String __DEFAULT_ENCODING = "ISO-8859-1";
    boolean _isAllowedToPost;
    int _replyCode;
    String _replyString;
    protected BufferedReader _reader_;
    protected BufferedWriter _writer_;
    protected ProtocolCommandSupport _commandSupport_;

    public CustomNNTP() {
        this.setDefaultPort(119);
        this._replyString = null;
        this._reader_ = null;
        this._writer_ = null;
        this._isAllowedToPost = false;
        this._commandSupport_ = new ProtocolCommandSupport(this);
    }

    private void __getReply() throws IOException {
        this._replyString = this._reader_.readLine();
        if(this._replyString == null) {
            throw new NNTPConnectionClosedException("Connection closed without indication.");
        } else if(this._replyString.length() < 3) {
            throw new MalformedServerReplyException("Truncated server reply: " + this._replyString);
        } else {
            try {
                this._replyCode = Integer.parseInt(this._replyString.substring(0, 3));
            } catch (NumberFormatException var2) {
                throw new MalformedServerReplyException("Could not parse response code.\nServer Reply: " + this._replyString);
            }

            this.fireReplyReceived(this._replyCode, this._replyString + "\r\n");
            if(this._replyCode == 400) {
                throw new NNTPConnectionClosedException("NNTP response 400 received.  Server closed connection.");
            }
        }
    }

    public void setCustomEncoding(String encoding) {
        __DEFAULT_ENCODING = encoding;
    }

    protected void _connectAction_() throws IOException {
        super._connectAction_();
        this._reader_ = new CRLFLineReader(new InputStreamReader(this._input_, __DEFAULT_ENCODING));
        this._writer_ = new BufferedWriter(new OutputStreamWriter(this._output_, __DEFAULT_ENCODING));
        this.__getReply();
        this._isAllowedToPost = this._replyCode == 200;
    }

    public void disconnect() throws IOException {
        super.disconnect();
        this._reader_ = null;
        this._writer_ = null;
        this._replyString = null;
        this._isAllowedToPost = false;
    }

    public boolean isAllowedToPost() {
        return this._isAllowedToPost;
    }

    public int sendCommand(String command, String args) throws IOException {
        StringBuilder __commandBuffer = new StringBuilder();
        __commandBuffer.append(command);
        if(args != null) {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }

        __commandBuffer.append("\r\n");
        String message;
        this._writer_.write(message = __commandBuffer.toString());
        this._writer_.flush();
        this.fireCommandSent(command, message);
        this.__getReply();
        return this._replyCode;
    }

    public int sendCommand(int command, String args) throws IOException {
        return this.sendCommand(NNTPCommand.getCommand(command), args);
    }

    public int sendCommand(String command) throws IOException {
        return this.sendCommand(command, (String)null);
    }

    public int sendCommand(int command) throws IOException {
        return this.sendCommand(command, (String)null);
    }

    public int getReplyCode() {
        return this._replyCode;
    }

    public int getReply() throws IOException {
        this.__getReply();
        return this._replyCode;
    }

    public String getReplyString() {
        return this._replyString;
    }

    public int article(String messageId) throws IOException {
        return this.sendCommand(0, messageId);
    }

    public int article(long articleNumber) throws IOException {
        return this.sendCommand(0, Long.toString(articleNumber));
    }

    public int article() throws IOException {
        return this.sendCommand(0);
    }

    public int body(String messageId) throws IOException {
        return this.sendCommand(1, messageId);
    }

    public int body(long articleNumber) throws IOException {
        return this.sendCommand(1, Long.toString(articleNumber));
    }

    public int body() throws IOException {
        return this.sendCommand(1);
    }

    public int head(String messageId) throws IOException {
        return this.sendCommand(3, messageId);
    }

    public int head(long articleNumber) throws IOException {
        return this.sendCommand(3, Long.toString(articleNumber));
    }

    public int head() throws IOException {
        return this.sendCommand(3);
    }

    public int stat(String messageId) throws IOException {
        return this.sendCommand(14, messageId);
    }

    public int stat(long articleNumber) throws IOException {
        return this.sendCommand(14, Long.toString(articleNumber));
    }

    public int stat() throws IOException {
        return this.sendCommand(14);
    }

    public int group(String newsgroup) throws IOException {
        return this.sendCommand(2, newsgroup);
    }

    public int help() throws IOException {
        return this.sendCommand(4);
    }

    public int ihave(String messageId) throws IOException {
        return this.sendCommand(5, messageId);
    }

    public int last() throws IOException {
        return this.sendCommand(6);
    }

    public int list() throws IOException {
        return this.sendCommand(7);
    }

    public int next() throws IOException {
        return this.sendCommand(10);
    }

    public int newgroups(String date, String time, boolean GMT, String distributions) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(date);
        buffer.append(' ');
        buffer.append(time);
        if(GMT) {
            buffer.append(' ');
            buffer.append("GMT");
        }

        if(distributions != null) {
            buffer.append(" <");
            buffer.append(distributions);
            buffer.append('>');
        }

        return this.sendCommand(8, buffer.toString());
    }

    public int newnews(String newsgroups, String date, String time, boolean GMT, String distributions) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(newsgroups);
        buffer.append(' ');
        buffer.append(date);
        buffer.append(' ');
        buffer.append(time);
        if(GMT) {
            buffer.append(' ');
            buffer.append("GMT");
        }

        if(distributions != null) {
            buffer.append(" <");
            buffer.append(distributions);
            buffer.append('>');
        }

        return this.sendCommand(9, buffer.toString());
    }

    public int post() throws IOException {
        return this.sendCommand(11);
    }

    public int quit() throws IOException {
        return this.sendCommand(12);
    }

    public int authinfoUser(String username) throws IOException {
        String userParameter = "USER " + username;
        return this.sendCommand(15, userParameter);
    }

    public int authinfoPass(String password) throws IOException {
        String passParameter = "PASS " + password;
        return this.sendCommand(15, passParameter);
    }

    public int xover(String selectedArticles) throws IOException {
        return this.sendCommand(16, selectedArticles);
    }

    public int xhdr(String header, String selectedArticles) throws IOException {
        StringBuilder command = new StringBuilder(header);
        command.append(" ");
        command.append(selectedArticles);
        return this.sendCommand(17, command.toString());
    }

    public int listActive(String wildmat) throws IOException {
        StringBuilder command = new StringBuilder("ACTIVE ");
        command.append(wildmat);
        return this.sendCommand(7, command.toString());
    }

    /** @deprecated */
    @Deprecated
    public int article(int a) throws IOException {
        return this.article((long)a);
    }

    /** @deprecated */
    @Deprecated
    public int body(int a) throws IOException {
        return this.body((long)a);
    }

    /** @deprecated */
    @Deprecated
    public int head(int a) throws IOException {
        return this.head((long)a);
    }

    /** @deprecated */
    @Deprecated
    public int stat(int a) throws IOException {
        return this.stat((long)a);
    }

    protected ProtocolCommandSupport getCommandSupport() {
        return this._commandSupport_;
    }
}

