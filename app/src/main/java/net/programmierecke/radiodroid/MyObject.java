package net.programmierecke.radiodroid; 

import android.util.Log;
 
public class MyObject {
 
    // for our logs
    public static final String TAG = "MyObject.java";
    
    public String objectName;
 
    // constructor for adding sample data
    public MyObject(String objectName){
    	// Log.e(TAG, "MyObject: " + objectName);
        this.objectName = objectName;
    }
 
}
