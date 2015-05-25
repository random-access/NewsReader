package org.random_access.newsreader;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Project: FlashCards Manager for Android
 * Date: 23.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class EditSubscriptionsFragment extends Fragment{

    private ArrayList<EditSubscriptionsActivity.NewsGroupItem> newsGroupItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public ArrayList<EditSubscriptionsActivity.NewsGroupItem> getNewsGroupItems() {
        return newsGroupItems;
    }

    public void setNewsGroupItems(ArrayList<EditSubscriptionsActivity.NewsGroupItem> newsGroupItems) {
        this.newsGroupItems = newsGroupItems;
    }
}
