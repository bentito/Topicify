/*
Licensed by AT&T under 'Software Development Kit Tools Agreement' 2012.
TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION: http://developer.att.com/sdk_agreement/
Copyright 2012 AT&T Intellectual Property. All rights reserved. 
For more information contact developer.support@att.com http://developer.att.com
*/
package edu.dartmouth.topicify;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.dartmouth.topicify.R;

/**
 * A simple activity launcher.
**/
// TODO: Bring over WAV saving code from MainTopicifyActivity and see how to get it in form AT&T wants to process bytes.
public class MainActivity extends ListActivity
{
    /**
     * Display a list of sample activities.
    **/
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        // Display the list of sample activities in a standard Android layout.
        activities = new Item[] {
            new Item(new Intent(this, SpeechActivity.class), 
                    getString(R.string.app_name_activity)),
            new Item(new Intent(this, SpeechService.class), 
                    getString(R.string.app_name_service))
        };
        setListAdapter(new ArrayAdapter<Item>(this, 
                android.R.layout.simple_list_item_1, activities));
    }
    
    /**
     * Represents an activity for the list view.
    **/
    private class Item {
        final Intent intent;
        final String title;
        Item(Intent intent, String title) {
            this.intent = intent; this.title = title;
        }
        @Override public String toString() {
            return title;
        }
    }

    private Item[] activities;
    
    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        startActivity(activities[position].intent);
    }
}
