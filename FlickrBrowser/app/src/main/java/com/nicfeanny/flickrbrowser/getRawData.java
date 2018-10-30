package com.nicfeanny.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus{IDLE, PROCESSING, NOT_INITIALIZED, FAILED_OR_EMPTY, OK}


class GetRawData extends AsyncTask<String, Void, String>{
    private static final String TAG = "GetRawData";

    private DownloadStatus downloadStatus;
    private final OnDownLoadComplete onCallBack;


    /*Creating an interface is like creating a class
    * however, you do not define the functions; that is up to the user
    * Instead, you are basically telling the user "These are the functions
    * I require in order for you to use me in your class"*/
    interface OnDownLoadComplete{
        void onDownloadComplete(String s, DownloadStatus status);
    }

    public GetRawData(OnDownLoadComplete callBack){
        this.downloadStatus = DownloadStatus.IDLE;
        this.onCallBack = callBack;
    }

    /*
    * "@OVerride is an annotation which tells the Java Compiler that the
    * method that follow is overriding an existing method in its superclass
    * or an interface.
     *--Using @override allows the compiler to check that the method has the correct name
    *   and the correct number and type of parameters
    * --Android Studio also uses it to show those error and warnings in the
    *   right hand margin.
    * --The code runs fine without it but its a good Idea to use it
    * */
    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: the parameter = " + s);
        super.onPostExecute(s);

        /*
        * this is using the callback function
        * this is how callbacks work, the called object will call when something
        * interesting happens, then you give the object a reference to something that
        * has that method in this case we gave the getRawData instance a reference
        * to the mainactivity object that Android creates when it launches the acitivity by
        * using this from inside the mainactivity; That is why we pass "this" in the
        * "GetRawData" object instantiation in MainActivity.
        * */
        if(onCallBack != null){
            onCallBack.onDownloadComplete(s, downloadStatus);
        }
        Log.d(TAG, "onPostExecute: ends");

    }

    /*
    *  This does what the AsyncTask .execute function does.
    *  when you call the execute method of an AsyncTask
    *  it creates a new thread and runs the doinBackground method.
    *  When that completes, the onPostExecutes function is called On the
    *  main thread.
    *   We just need to do the same thing minus creating a new background thread
    *   so we call the doInBackground method and return the value from that to onPostExecute.
    *
    *
    *
    * */
    void runInSameThread(String s){
        Log.d(TAG, "onInSameThread: Starts...");

     //   onPostExecute(doInBackground(s));
          if(onCallBack != null){
           //   String result = doInBackground(s);
           //   onCallBack.onDownloadComplete(result, downloadStatus);
              onCallBack.onDownloadComplete(doInBackground(s), downloadStatus);
          }


        Log.d(TAG, "onInSameThread: Ends...");
    }

    //Strings...strings is function overloading in java, i.e., there could be one argument or 100
    //you access that argument like an array with [<index>]
    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        //check to see if we have been given a url
        //if not we have a problum
        if(strings == null){
            downloadStatus = DownloadStatus.NOT_INITIALIZED;
            return null;
        }

        try{
           //set to processing as... we have begun to retrieve the data from the api pull
            downloadStatus = DownloadStatus.PROCESSING;

            //we create a URL via the strings parameter (argument passed in)
            URL url = new URL(strings[0]);
            StringBuilder result = new StringBuilder();
            String line;

            //URL string is used to open a connection
            connection = (HttpURLConnection) url.openConnection();

            //this statement is a http request
            //we can GET,PUT,POST and request the information to be retrieved from or pushed
            //to the server, in this case, we are requesting that the API goes and GETS
            //the information we are asking for
            //FYI: sometimes you might see HttpURLConnections without a setRequest
            //this is because GET is set by default (we didn't have to do this in the top 10 app)
            connection.setRequestMethod("GET");
            
            connection.connect();

            //we log to verify if the connection was successsful or if there was an error
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: The response code was " + response);

            //we are using a buffered reader to read data in chunks
            //we fill the buffer, parse it and then store it, i.e., append,
            //rinse and repeat, however in this case we are reading a line at a time
            //but the mindset remains the same as there are many lines of XML/JSON
            //reading a line at a time is better than reading by individual characters and
            //placing that into the buffered reader
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //we have to read a line at a time
            //the newline is stripped off so we have to put a new newline to make sure
            //it stays in the same format
            while((line = reader.readLine()) != null){
                result.append(line).append("\n");
            }

            //alternative format to the above loop
            //for(String line = reader.readLine(); line != null; line = reader.readLine())
            //{result.append(line).append("\n")}

            //we then set the status to ok (this is obvious, greg) and then we return
            //that string we created in all it's glory
            downloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch(MalformedURLException e){
            //i.e., url not in the right format
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch(IOException e){
            //exception is thrown because of IO...
            Log.e(TAG, "doInBackground: IO Exception reading data: " + e.getMessage());
        } catch(SecurityException e){
            //thrown if we do not have the correct permissions to access the internet
            Log.e(TAG, "doInBackground: Security Exception. Needs Permission?" + e.getMessage() );
        } finally{
            //once we are done retrieve the data from the request
            //we disconnect; kind of like deallocating
            if(connection != null){
                connection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch(IOException e){
                    Log.e(TAG, "doInBackground: Error closing stream " + e.getMessage());
                }
            }
        }


        //if we get here something went wrong with parsing process
        //in other words, something went wrong when we attempted to grab the raw data from the api
        downloadStatus = DownloadStatus.FAILED_OR_EMPTY;

        return null;
    }



}
