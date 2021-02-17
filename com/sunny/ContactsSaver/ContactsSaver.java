package com.sunny.ContactsSaver;
import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;

@DesignerComponent(version = 1,
        description = "Add contacts without using activity starter<br>Developed by <a href=https://sunnythedeveloper.epizy.com>Sunny Gupta</a>",
        nonVisible = true,
        iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png",
        category = ComponentCategory.EXTENSION)
@SimpleObject(external=true)
@UsesPermissions(permissionNames = "android.permission.READ_CONTACTS,android.permission.WRITE_CONTACTS")
public class ContactsSaver extends AndroidNonvisibleComponent{
    public Activity activity;
    public Context context;
    public ContactsSaver(ComponentContainer container){
        super(container.$form());
        context = container.$context();
        activity = (Activity) context;
    }
    @SimpleFunction(description = "Creates a new contact with given values and triggers \"ContactCreated\" event with success and rawContactId")
    public void SaveContact(final YailDictionary values){
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues value = new ContentValues();
                    for (Object k : values.keySet()) {
                        value.put(k.toString(), values.get(k).toString());
                    }
                    Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, value);
                    long rawContactId = ContentUris.parseId(rawContactUri);
                    postResult(true, String.valueOf(rawContactId));
                }catch (Exception e){
                    e.printStackTrace();
                    postResult(false,e.getMessage()!=null?e.getMessage():e.toString());
                }
            }
        });
    }
    public void postResult(final boolean successful,final String result){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContactCreated(successful,result);
            }
        });
    }
    /*@SimpleFunction(description = "Gets rawContactId from phone number")
    public String GetRawContactId(String phoneNumber){
    	ContentResolver contentResolver = context.getContentResolver();
    	Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;
    	String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, 
    										ContactsContract.CommonDataKinds.Phone.NUMBER}
        Cursor cursor = contentResolver.query(
                uri,
                projection,
                ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                new String[]{phoneNumber},
                null
        );
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));
    }*/
    @SimpleFunction(description = "Adds data to given contact")
    public void AddData(final String mimeType,final YailDictionary values,final String rawContactId){
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues value = new ContentValues();
                    for (Object k : values.keySet()) {
                        value.put(k.toString(), values.get(k).toString());
                    }
                    value.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
                    value.put(ContactsContract.Data.MIMETYPE,mimeType);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, value);
                    postResult(true);
                }catch (Exception e){
                    e.printStackTrace();
                    postResult(false);
                }
            }
        });
    }
    public void postResult(final boolean successful){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DataAdded(successful);
            }
        });
    }
    @SimpleEvent(description = "Event raised after 'SaveContact' method")
    public void ContactCreated(boolean successful,String result){
        EventDispatcher.dispatchEvent(this,"ContactCreated",successful,result);
    }
    @SimpleEvent(description = "Event raised after 'AddData' method")
    public void DataAdded(boolean successful){
        EventDispatcher.dispatchEvent(this,"DataAdded",successful);
    }
}
