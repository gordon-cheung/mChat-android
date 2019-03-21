package com.example.macbook.mchat;

import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.widget.*;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// TODO Cleanup Chat activity
public class ChatActivity extends MChatActivity {
    private String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private String contactId;
    private ArrayList<String> mPictures = new ArrayList<String>();

    private final static int GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO aggregate Contact with ChatActivity
        contactId = getIntent().getExtras().getString(AppNotification.CONTACT_DATA);
        getSupportActionBar().setTitle(contactId);

        // TODO remove initial messageList
        ArrayList<Message> messageList =  new ArrayList<Message>();

        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        mRecyclerView = findViewById(R.id.reyclerview_message_list);
        // TODO update user list
        mAdapter = new ChatAdapter(displaySize.x, displaySize.y);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new GetMessagesTask().execute();

        final Button sendButton = findViewById(R.id.button_chatbox_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText editText = findViewById(R.id.edittext_chatbox);
                String message = editText.getText().toString();

                // TODO use file path for images
                for (String imageFilePath : mPictures) {
                    sendPictureMessage(imageFilePath);
                }

                mPictures.clear();
                findViewById(R.id.layout_picture).setVisibility(View.GONE);
                TextView pictureText = findViewById(R.id.textview_picture);
                pictureText.setText(mPictures.size() + " images attached");

                if (!message.isEmpty()) {
                    sendMessage(new Message(message, contactId, Message.IS_SEND, Message.TEXT));
                    editText.setText("");
                }
            }
        });

        final ImageButton pictureButton = findViewById(R.id.button_picture);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                intent.setType("image/*");
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, GALLERY);
            }
        });

        final AppCompatImageButton removePicButton = findViewById(R.id.button_picture_remove);
        removePicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPictures.clear();

                LinearLayout layout = findViewById(R.id.layout_picture);
                layout.setVisibility(View.GONE);
                TextView pictureText = findViewById(R.id.textview_picture);
                pictureText.setText("");
            }
        });

        // TODO: Remove
        final Button testButton = findViewById(R.id.button_chatbox_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "TEST BUTTON CLICKED");

                // Enable this to test startNetworkRegistration
                //mBluetoothService.startNetworkRegistration();

                // Enable this to test broadcast notification
                Intent broadcastTestIntent = new Intent(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
                Message message = new Message("/storage/emulated/0/DCIM/Camera/20190318_224056.jpg", contactId, Message.IS_RECEIVE, Message.PICTURE, System.currentTimeMillis());
                broadcastTestIntent.putExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION, message);
                sendBroadcast(broadcastTestIntent);
            }
        });

//        try {
//            Display display = getWindowManager().getDefaultDisplay();
//            Point size = new Point();
//            display.getSize(size);
//            ImageView imageView = (ImageView) findViewById(R.id.picture_message_body);
//            Uri mUri = Uri.fromFile(new File("/storage/emulated/0/DCIM/Camera/20190318_224056.jpg"));
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mUri);
//            bitmap = getThumbnail(bitmap, size.x / 2, size.y / 2);
//            int byteCount = bitmap.getByteCount();
//            imageView.setImageBitmap(bitmap);
//            imageView.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Log.d(TAG, "TEST BUTTON CLICKED");
//
//                    // Enable this to test startNetworkRegistration
//                    //mBluetoothService.startNetworkRegistration();
//                    Uri mUri = Uri.fromFile(new File("/storage/emulated/0/DCIM/Camera/20190318_224056.jpg"));
//                    // Enable this to test broadcast notification
//                    Intent intent = new Intent(getApplicationContext(), FullImageViewActivity.class);
//                    startActivity(intent);
//                }
//            });
//        } catch(Exception ex) {
//            Log.d(TAG, ex.getMessage());
//        }

    }

    private boolean sendMessage(final Message msg) {
        int currentSize = mAdapter.getItemCount();

        // Update UI
        mAdapter.addMessage(msg);
        mAdapter.notifyItemInserted(currentSize);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        // Store message in database
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
            Log.d(TAG, "Inserting stored sent message");
            AppDatabase.getInstance().messageDao().insert(msg);
            }
        });

        // TODO handle messages failed to send
        if (mBluetoothService.send(msg)) {
            Log.d(TAG, "Message successfully sent");
        } else {
            Log.e(TAG, "Message failed to send");
        }
        return true;
    }

    private boolean sendPictureMessage(final String imageFilePath) {
        int currentSize = mAdapter.getItemCount();

        // TODO show image as view
        // How to do this and make it an intent to show image when clicked
        final Message msg = new Message(imageFilePath, contactId, Message.IS_SEND, Message.PICTURE);
        // Update UI
        mAdapter.addMessage(msg);
        mAdapter.notifyItemInserted(currentSize);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        // TODO store image correctly (use file path)
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "Inserting stored sent message");
//                AppDatabase.getInstance().messageDao().insert(msg);
//            }
//        });
//
//        if (mBluetoothService.send(msg)) {
//            Log.d(TAG, "Message successfully sent");
//        } else {
//            Log.e(TAG, "Message failed to send");
//            return false;
//        }

        return true;
    }

    private boolean ReceiveMessage(final Message msg) {
        Log.d(TAG, "Message received: " + msg.getBody() + " from user: " + msg.getContactId());
        int currentSize = mAdapter.getItemCount();

        mAdapter.addMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "Inserting stored received message");
//                mAppDatabase.messageDao().insert(msg);
//            }
//        });

        return true;
    }

    @Override
    public void onAppNotificationReceived(Intent intent) {
        final String action = intent.getAction();
        if (action == AppNotification.MESSAGE_RECEIVED_NOTIFICATION) {
            Log.d(TAG, "Message Received");
            Message msg = (Message)intent.getSerializableExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
            if (msg.getContactId().equals(contactId)) {
                ReceiveMessage(msg);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    String filePath = getPath(contentURI);
                    // TODO Store the path instead?
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    mPictures.add(filePath);
                    findViewById(R.id.layout_picture).setVisibility(View.VISIBLE);

                    TextView pictureText = findViewById(R.id.textview_picture);
                    if (mPictures.size() > 1) {
                        pictureText.setText(mPictures.size() + " images attached");
                    }
                    else {
                        pictureText.setText(mPictures.size() + " image attached");
                    }
                    pictureText.setText(mPictures.size() + " image attached");

                } catch (Exception e) { //IOException
                    Log.e(TAG, "Error getting image");
                }
            }

        }
    }

    private Bitmap getThumbnail(Bitmap image, float maxWidth, float maxHeight) {
        float width = image.getWidth();
        float height = image.getHeight();
        float ratio = width/height;

        if (width >= height) {
            if (width > maxWidth) {
                width = maxWidth;
                height = maxWidth / ratio;
            }
        }
        else {
            if (height > maxHeight) {
                height = maxHeight;
                width = maxHeight * ratio;
            }
        }

        return ThumbnailUtils.extractThumbnail(image, (int)width, (int)height);
    }

    private String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    private class GetMessagesTask extends AsyncTask<Void, Void, List<Message>>
    {
        @Override
        protected List<Message> doInBackground(Void... voids) {
            return AppDatabase.getInstance().messageDao().getAll(contactId);
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            for (Message msg: messages) {
                Log.d(TAG, String.format("ContactId %s, MessageBody %s", msg.getContactId(), msg.getBody()));
                int currentSize = mAdapter.getItemCount();

                mAdapter.addMessage(msg);
                mAdapter.notifyItemInserted(currentSize);

                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }
}
