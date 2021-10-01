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
        versionName = "1.1",
        description = "Save/Update/Delete contacts without using activity starter<br>Developed by <a href=https://sunnythedeveloper.xyz>Sunny Gupta</a>",
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
                    postCreateResult(true, String.valueOf(rawContactId));
                }catch (Exception e){
                    e.printStackTrace();
                    postCreateResult(false,e.getMessage()!=null?e.getMessage():e.toString());
                }
            }
        });
    }
    private void postCreateResult(final boolean successful, final String result){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContactCreated(successful,result);
            }
        });
    }
    @SimpleFunction(description = "Get raw contact id from phone number")
    public String GetRawContactId(String phoneNumber){
    	ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
    	String[] projection = new String[]{ContactsContract.PhoneLookup._ID};
        Cursor cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()){
            return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
        }
        return "";
    }
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
                    postAddResult(true);
                }catch (Exception e){
                    e.printStackTrace();
                    postError(e.getMessage());
                    postAddResult(false);
                }
            }
        });
    }
    private void postAddResult(final boolean successful){
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
    @SimpleEvent(description = "Event raised after 'AddData' method and returns result")
    public void DataAdded(boolean successful){
        EventDispatcher.dispatchEvent(this,"DataAdded",successful);
    }
    @SimpleFunction(description = "Updates data in given contact")
    public void UpdateData(final String mimeType,final YailDictionary values,final String rawContactId){
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues value = new ContentValues();
                    for (Object k : values.keySet()) {
                        value.put(k.toString(), values.get(k).toString());
                    }
                    context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
                            value,
                            ContactsContract.Data.RAW_CONTACT_ID + "=" + rawContactId
                            + " and " + ContactsContract.Data.MIMETYPE + "=" + mimeType,
                            null);
                    postUpdateResult(true);
                }catch (Exception e){
                    e.printStackTrace();
                    postError(e.getMessage());
                    postUpdateResult(false);
                }
            }
        });
    }
    private void postUpdateResult(final boolean successful){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DataUpdated(successful);
            }
        });
    }
    @SimpleEvent(description = "Event raised after 'UpdateData' method and returns result")
    public void DataUpdated(boolean successful){
        EventDispatcher.dispatchEvent(this,"DataUpdated",successful);
    }
    @SimpleFunction(description = "Deletes given contact")
    public void DeleteContact(final String rawContactId){
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    context.getContentResolver().delete(ContactsContract.Data.CONTENT_URI,
                            ContactsContract.Data.RAW_CONTACT_ID + "=" + rawContactId,
                            null);
                    context.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,
                            ContactsContract.RawContacts._ID + "=" + rawContactId,
                            null);
                    context.getContentResolver().delete(ContactsContract.Contacts.CONTENT_URI,
                            ContactsContract.Contacts._ID + "=" + rawContactId,
                            null);
                    postDeleteResult(true);
                }catch (Exception e){
                    e.printStackTrace();
                    postError(e.getMessage());
                    postDeleteResult(false);
                }
            }
        });
    }
    private void postDeleteResult(final boolean successful){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContactDeleted(successful);
            }
        });
    }
    @SimpleEvent(description = "Event raised after 'DeleteContact' method and returns result")
    public void ContactDeleted(boolean successful){
        EventDispatcher.dispatchEvent(this,"ContactDeleted",successful);
    }
    private void postError(final String e){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GotError(e);
            }
        });
    }
    @SimpleEvent()
    public void GotError(String errorMessage){
        EventDispatcher.dispatchEvent(this,"GotError",errorMessage);
    }
}
