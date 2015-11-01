package net.programmierecke.radiodroid;
 
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
 
public class CustomAutoCompleteTextChangedListener implements TextWatcher{
 
    public static final String TAG = "CustomAutoCompleteTextChangedListener.java";
    Context context;
     
    public CustomAutoCompleteTextChangedListener(Context context){
        this.context = context;
    }
     
    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
         
    }
 
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // TODO Auto-generated method stub
         
    }
 
    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {
 
        // if you want to see in the logcat what the user types
        // Log.e(TAG, "User input: " + userInput);
 
        MainActivity2 mainActivity2 = ((MainActivity2) context);
         
        // query the database based on the user input
        mainActivity2.item = mainActivity2.getItemsFromDb(userInput.toString());
        
        // Log.e(TAG, "database => " + mainActivity2.item);
         
        // update the adapter
        
        // android autocomplete color bug
        // see https://code.google.com/p/android/issues/detail?id=5237
        // fixed by AutoCompleteTextView has white text color when setting Theme_Light programatically.
        // see http://stackoverflow.com/questions/11787057/autocompletetextview-background-foreground-color
        
        mainActivity2.myAdapter.notifyDataSetChanged();
        // mainActivity2.myAdapter = new ArrayAdapter<String>(mainActivity2, android.R.layout.simple_dropdown_item_1line, mainActivity2.item);
        mainActivity2.myAdapter = new ArrayAdapter<String>(mainActivity2, android.R.layout.simple_list_item_1, mainActivity2.item);
        mainActivity2.myAutoComplete.setAdapter(mainActivity2.myAdapter);
         
    }
 
}
