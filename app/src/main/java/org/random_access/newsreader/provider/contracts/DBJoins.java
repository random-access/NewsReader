package org.random_access.newsreader.provider.contracts;

import android.net.Uri;

import org.random_access.newsreader.provider.NNTPProvider;

/**
 * <b>Project:</b> Newsreader for Android <br>
 * <b>Date:</b> 01.07.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class DBJoins {

    public static final String MESSAGE_JOIN_MESSAGEHIERARCHY_CHILDREN = "MESSAGE_JOIN_MESSAGEHIERARCHY_CHILDREN";
    public static final Uri CONTENT_URI_MESSAGE_JOIN_MESSAGEHIERARCHY_CHILDREN = Uri.parse("content://" + NNTPProvider.AUTHORITY + "/" + MESSAGE_JOIN_MESSAGEHIERARCHY_CHILDREN);
    public static final String TABLES_MESSAGE_JOIN_MESSAGEHIERARCHY_CHILDREN = MessageContract.TABLE_NAME + " inner join " + MessageHierarchyContract.TABLE_NAME
            + " on " + MessageContract.MessageEntry.COL_ID_FULLNAME + " = " + MessageHierarchyContract.MessageHierarchyEntry.COL_MSG_DB_ID_FULLNAME;


    public static final String MESSAGE_JOIN_MESSAGEHIERARCHY_PARENT = "MESSAGE_JOIN_MESSAGEHIERARCHY_ROOT";
    public static final Uri CONTENT_URI_MESSAGE_JOIN_MESSAGEHIERARCHY_ROOT = Uri.parse("content://" + NNTPProvider.AUTHORITY + "/" + MESSAGE_JOIN_MESSAGEHIERARCHY_PARENT);
    public static final String TABLES_MESSAGE_JOIN_MESSAGEHIERARCHY_PARENT = MessageContract.TABLE_NAME + " inner join " + MessageHierarchyContract.TABLE_NAME
            + " on " + MessageContract.MessageEntry.COL_ID_FULLNAME + " = " + MessageHierarchyContract.MessageHierarchyEntry.COL_IN_REPLY_TO_FULLNAME;

}
