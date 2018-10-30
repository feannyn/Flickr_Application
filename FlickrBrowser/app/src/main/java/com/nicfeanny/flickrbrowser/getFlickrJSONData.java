package com.nicfeanny.flickrbrowser;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
*  packaged private upon creation of the class
*  although this does not extend AsyncData, it does implement
*   GetRawJSONData which does run asynchronously on a background
*   Thread; THis means that anything using this getFlickrJSONData
*   Class wont get any data back immediately. To resolve this,
*   we are going to use the same callback mechanism as we did
*   for getRawData. So we are going to use the sme callback mechanism
*   as we did for getRawData. So we will create a field to store
*   the callback object and then define an interface which will call
*   that private
* */

//This asyncTasks contains the query we want to use to actually filter the flickr feed
//The first parameter is going to be a string -> the same parameter thart we currently
// are parsing to execute on the same
//the second parameter -> is Void because we do not want to display any type of progress bar
//the third-> The list of photos we will be returning
class GetFlickrJSONData extends AsyncTask<String, Void, List<Photo>> implements GetRawData.OnDownLoadComplete{
    private static final String TAG = "getFlickrJSONData";


    private List<Photo> PhotoList = null;
    private String baseURL; //the url prior to adding parameters to pull JSON object
    private String language; //different languages (English, Spanish, German, French etc.)
    private boolean matchAll;

    private final OnDataAvailable callBack;
    private boolean runningOnSameThread = false;


    /*
    * This class implements OnDownloadComplete so itr can get callbacks from
    * getRawData and it also defines its own interface OnDataAvailable so that it
    * can send a callback to MainActivity
    *
    * */
    interface OnDataAvailable {
        void onDataAvailable(List<Photo> data, DownloadStatus status);
    }

    //constructor
    public GetFlickrJSONData(OnDataAvailable callBack, String baseURL, String language, boolean matchAll) {
        this.baseURL = baseURL;
        this.language = language;
        this.matchAll = matchAll;
        this.callBack = callBack;
    }


    /*
    *   most of the code here is similar to what is being done in mainactivities
    *   OnCreate method where it creates a new GetRawData object and then calls
    *   the execute method
    *   However, before the above is done we are going to Create a URI with the
    *   correct parameters via a creaURI method.
    * */
    void executeOnSameThread(String searchCriteria){
        Log.d(TAG, "executeOnSameThread: starts...");
        runningOnSameThread = true;
        String destinationURI = createUri(searchCriteria, language, matchAll);
    
        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(destinationURI);
        Log.d(TAG, "executeOnSameThread: ends...");
    
    }


    @Override
    protected void onPostExecute(List<Photo> photos) {
        Log.d(TAG, "onPostExecute: Starts...");

        if(callBack != null){
            callBack.onDataAvailable(PhotoList, DownloadStatus.OK);
        }

        Log.d(TAG, "onPostExecute: Ends...");
    }


    @Override
    protected List<Photo> doInBackground(String... params) {
        Log.d(TAG, "doInBackground: Starts...");

        String destinationURL = createUri(params[0], language, matchAll);

        GetRawData getRawData = new GetRawData(this);

        getRawData.runInSameThread(destinationURL);

        Log.d(TAG, "doInBackground: Ends...");

        return PhotoList;
    }

    private String createUri(String searchCriteria, String lang, boolean mAll){
        Log.d(TAG, "createUri: starts");

        Uri uri = Uri.parse(baseURL);
        Uri.Builder builder = uri.buildUpon();


        /*
        * This is a chain method call in order to do this without chaining
        *   you would have to call a variable and class build and repeat what is below
        *   minus the chaining aspect.
        *
        *
        * */
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("tags", searchCriteria)
                .appendQueryParameter("tagmode", matchAll ? "ALL":"ANY")
                .appendQueryParameter("lang", lang)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .build().toString();
    }

    //USING ORG.JSON Library
    //data is the string we created to represent the jsonObject in our code
    //this is what we will be parsing
    //status is verifying that it was successful in creating the "JSON OBject String (data)"
    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        Log.d(TAG, "onDownloadComplete: starts... Status = " + status);


        //we are going to create an arraylist which will hold the photos and the other
        //parsed json info.
        if(status == DownloadStatus.OK){
            PhotoList = new ArrayList<>();

            try{
                //JSONArray is an item is going to identify a particular photo in the JSON Object
                //Basically... this targets an array within the JSON Object we are currently parsing.
                //nd within that array are more JSON objects with various fields
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("items");

                /*
                * photoUrl will become thew image field of the photo object and is passed as the last
                *   parameter to the constructor and if we have a look at the photo class, the last
                *   parameter in the constructor sets the value of the image so we'll be using that
                *   field to display the image for each photo in the list
                *
                * When an item in the list is tapped, we are going to launch another activity to
                *   display the photo much larger so that it fills the screen.
                *   To do this, we need to use the link value; which is why it is separate
                *   to the photo url so getImage will give us the URL of the photo to show in the
                *   initial list and getLink will provide the URL of the full-size picture
                *   !!!!FYI these functions discussed above are both below and in the photo class
                *
                *
                * */
                for(int i = 0; i < itemsArray.length(); i++){
                    JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                    String title = jsonPhoto.getString("title");
                    String author = jsonPhoto.getString("author");
                    String authorID = jsonPhoto.getString("author_id");
                    String tags = jsonPhoto.getString("tags");

                    JSONObject jsonMedia = jsonPhoto.getJSONObject("media");

                    //This image (photoUrl) will display in the recycler view
                    //Then when you click that image the "link" below will display a larger image
                    String photoUrl = jsonMedia.getString("m");

                    //This will replace the current size attribute with the larger size attribute
                    String link = photoUrl.replaceFirst("_m.", "_b.");

                    //create a photo object
                    Photo photoObject = new Photo(title, author, authorID, link, tags, photoUrl);

                    //store this new photo object into the list
                    PhotoList.add(photoObject);

                    //logged here to verify everything has worked.
                    Log.d(TAG, "onDownloadComplete" + photoObject.toString());


                    //the code will keep looping until it has processed all the items in the
                    //JSON Array and we will end up with a list containing the details for each
                    //of the photos from the flickr feed


                    /*!!!!!!!This is JSON PARSING IN A NUTSHELL!!!!!!!*/
                }

            } catch (JSONException jsone){
                jsone.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Error processing JSON Data" + jsone.getMessage());
                status = DownloadStatus.FAILED_OR_EMPTY;
            }

            /*Once all of this is done we need to notify the calling class that everything
            * is done and send it the list of photos that we've actually created*/

            if(runningOnSameThread && callBack != null){
                //now inform the caller that processing is done == possibly returning null if there
                //was an error

                callBack.onDataAvailable(PhotoList, status);
            }

            Log.d(TAG, "onDownloadComplete: ends");

        }
    }
}
