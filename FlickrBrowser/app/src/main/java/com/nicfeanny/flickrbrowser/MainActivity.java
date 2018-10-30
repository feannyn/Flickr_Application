package com.nicfeanny.flickrbrowser;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.util.List;

/*MISC NOTES
*
* Interfaces-> An interface is like a contract in that the use of that interface
* requires the implementing class to define all the methods within that interface
* in order to use that the interface; otherwise you will get an error.
* --In the case of this particular Application, The interface is going to be created and
*   used to guarantee that MainActivity actually does have an onDownLoadComplete method;
*   In other words, we are going to create an interface that the callback object must implement
*
* --
*
*
* */

public class MainActivity extends AppCompatActivity implements GetFlickrJSONData.OnDataAvailable{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starts...");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        * We want to notify the calling class that the data has be processed and is ready to use
        * This is accomplished by the calling class which uses "GetRawData" to download the data
        * from some url. GetRawData then does all the work of downloading and storing that data.
        * when it is finished with this task it then calls the class back using what is known as a
        * callback method. This is kind of similar to a button object; The class that wants to know
        * when a button is clicked creates an object with a method to respond to that behavior by
        * passing said object to the button itself.
        * BASICALLY, when the data is done downloading we "listen" for the completion of this task
        * and then do something in response to that.
        * */
        //GetRawData getRawData = new GetRawData(this);
        //getRawData.execute("https://api.flickr.com/services/feeds/photos_public.gne?tags=android,nougat,sdk&tagmode=any&format=json&jsoncallback=1");

        Log.d(TAG, "onCreate: Ends...");
    }


    /*
     * onResume is overriding the previous functionality above (GETRAWDATA /.Execute)
     * This is done because because when we pause the current activity and return we want
     * to resume what we were initially doing.
     * */
     @Override
     protected void onResume(){
         Log.d(TAG, "onResume: starts...");
         super.onResume();
         GetFlickrJSONData getFlickrJSONData = new GetFlickrJSONData(this, "http://api.flickr.com/services/feeds/photos_public.gne", "en-us", true);
         getFlickrJSONData.execute("android, nougat");
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Log.d(TAG, "onCreateOptionsMenu: returned" + true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        Log.d(TAG, "onOptionsItemSelected: returned ");
        return super.onOptionsItemSelected(item);
    }


    //This method is created to verify that everything is working as expected
    //it will not be used once everything is deployed but is a mechanism created
    //to assist in the development process.
    public void onDataAvailable(List<Photo> data, DownloadStatus status){
        if(status == DownloadStatus.OK){
            Log.d(TAG, "onDownloadComplete: Data is " + data);
        } else{
            //download or processing failed
            Log.e(TAG, "onDownloadComplete failed with status " + status);
        }
    }
}
