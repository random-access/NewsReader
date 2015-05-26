package org.random_access.newsreader;

import android.app.Fragment;
import android.os.Bundle;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ShowSingleArticleFragment extends Fragment {

    private String[] articleData;
    private boolean extended;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public String[] getArticleData() {
        return articleData;
    }

    public void setArticleData(String[] articleData) {
        this.articleData = articleData;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }
}
