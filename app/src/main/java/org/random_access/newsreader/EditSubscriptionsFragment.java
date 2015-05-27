package org.random_access.newsreader;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * <b>Project:</b> FlashCards Manager for Android <br>
 * <b>Date:</b> 18.05.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class EditSubscriptionsFragment extends Fragment{

    private ArrayList<EditSubscriptionsActivity.NewsGroupItem> newsGroupItems;
    private int checkedSelection;

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

    public int getCheckedSelection() {
        return checkedSelection;
    }

    public void setCheckedSelection(int checkedSelection) {
        this.checkedSelection = checkedSelection;
    }
}
