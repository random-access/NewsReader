package org.random_access.newsreader.nntp;

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.io.DotTerminatedMessageReader;
import org.apache.commons.net.nntp.ArticleInfo;
import org.apache.commons.net.nntp.NNTPReply;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

 /**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 *
 * **** This class is a slightly modified version of the Apache Commons Net's NNTPClient class ****
 *              The license can be found here: http://www.apache.org/licenses/LICENSE-2.0
 */
public class CustomNNTPClient extends CustomNNTP {

    public BufferedReader retrieveArticleBody(String articleId, ArticleInfo pointer) throws IOException {
        return this.__retrieve(1, articleId, pointer);
    }

    public Reader retrieveArticleBody(String articleId) throws IOException {
        return this.retrieveArticleBody(articleId, (ArticleInfo) null);
    }

    private BufferedReader __retrieve(int command, String articleId, ArticleInfo pointer) throws IOException {
        if(articleId != null) {
            if(!NNTPReply.isPositiveCompletion(this.sendCommand(command, articleId))) {
                return null;
            }
        } else if(!NNTPReply.isPositiveCompletion(this.sendCommand(command))) {
            return null;
        }

        if(pointer != null) {
            this.__parseArticlePointer(this.getReplyString(), pointer);
        }

        return new DotTerminatedMessageReader(this._reader_);
    }

    private void __parseArticlePointer(String reply, ArticleInfo pointer) throws MalformedServerReplyException {
        String[] tokens = reply.split(" ");
        if(tokens.length >= 3) {
            byte i = 1;

            try {
                int var7 = i + 1;
                pointer.articleNumber = Long.parseLong(tokens[i]);
                pointer.articleId = tokens[var7++];
                return;
            } catch (NumberFormatException var6) {
                ;
            }
        }

        throw new MalformedServerReplyException("Could not parse article pointer.\nServer reply: " + reply);
    }

    public boolean authenticate(String username, String password) throws IOException {
        int replyCode = this.authinfoUser(username);
        if(replyCode == 381) {
            replyCode = this.authinfoPass(password);
            if(replyCode == 281) {
                this._isAllowedToPost = true;
                return true;
            }
        }

        return false;
    }

    public BufferedReader retrieveArticleHeader(String articleId, ArticleInfo pointer) throws IOException {
        return this.__retrieve(3, articleId, pointer);
    }

    public Reader retrieveArticleHeader(String articleId) throws IOException {
        return this.retrieveArticleHeader(articleId, (ArticleInfo)null);
    }


}
