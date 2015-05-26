package org.random_access.newsreader;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Project: FlashCards Manager for Android
 * Date: 26.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
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
