package org.random_access.newsreader;

import android.app.Fragment;
import android.os.Bundle;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 12.07.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ServerSettingsFragment extends Fragment{

    private String serverTitle;
    private String serverName;
    private String serverPort;
    private boolean encryption;
    private boolean auth;
    private String userName;
    private String password;
    private String userDisplayName;
    private String mailAddress;
    private String signature;
    private int chooseMsgLoadTimeIndex;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    public String getServerTitle() {
        return serverTitle;
    }

    public void setServerTitle(String serverTitle) {
        this.serverTitle = serverTitle;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public boolean hasEncryption() {
        return encryption;
    }

    public void setEncryption(boolean encryption) {
        this.encryption = encryption;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getChooseMsgLoadTimeIndex() {
        return chooseMsgLoadTimeIndex;
    }

    public void setChooseMsgLoadTimeIndex(int chooseMsgLoadTimeIndex) {
        this.chooseMsgLoadTimeIndex = chooseMsgLoadTimeIndex;
    }
}
