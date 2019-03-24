package com.example.macbook.mchat;

import android.Manifest;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.widget.*;
import android.view.View;

import java.io.*;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends MChatActivity {
    private String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    static int mMsgId = 0;

    // TODO Aggregate Contact to this class
    private String contactId;
    private ArrayList<String> mPictures = new ArrayList<String>();

    private final static int GALLERY = 1;

    static int incrementMessageId() {
        mMsgId++;
        if (mMsgId >= 65536){
            mMsgId = 0;
        }
        return mMsgId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO aggregate Contact with ChatActivity
        contactId = getIntent().getExtras().getString(AppNotification.CONTACT_DATA);
        getSupportActionBar().setTitle(contactId);

        Point displaySize = getDisplaySize();
        mAdapter = new ChatAdapter(displaySize.x, displaySize.y);

        mRecyclerView = findViewById(R.id.recycler_view_message_list);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new GetMessagesTask().execute();

        bindEvents();
    }

    private void bindEvents() {
        final Button sendButton = findViewById(R.id.button_chatbox_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText editText = findViewById(R.id.edittext_chatbox);
                String message = editText.getText().toString();

                for (String imageFilePath : mPictures) {
                    final Message msg = new Message(imageFilePath, contactId, Message.IS_SEND, Message.PICTURE, incrementMessageId());
                    sendMessage(msg);
                }

                clearImages();

                if (!message.isEmpty()) {
                    sendMessage(new Message(message, contactId, Message.IS_SEND, Message.TEXT, incrementMessageId()));
                    editText.setText("");
                }
            }
        });

        final ImageButton pictureButton = findViewById(R.id.button_picture);
        pictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, GALLERY);
                } else {
                    getPermissions();
                }
            }
        });

        final AppCompatImageButton removePicButton = findViewById(R.id.button_picture_remove);
        removePicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearImages();
            }
        });

        final EditText chatEditText = findViewById(R.id.edittext_chatbox);
        final TextWatcher textWatcher  = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().getBytes().length > Packet.PACKET_MAX_CONTENT_SIZE)  {
                    CharSequence tb = s.subSequence(0, Packet.PACKET_MAX_CONTENT_SIZE);
                    chatEditText.removeTextChangedListener(this);
                    chatEditText.setText(tb);
                    chatEditText.setSelection(tb.length());
                    chatEditText.addTextChangedListener(this);
                    Toast.makeText(getApplicationContext(), "Maximum text message length reached", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        chatEditText.addTextChangedListener(textWatcher);

        // TODO: Remove
        final Button testButton = findViewById(R.id.button_chatbox_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "TEST BUTTON CLICKED");

                try {
                    BluetoothService bs = new BluetoothService();
                    // TODO add code below to t est function to test
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_image1);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);

                    Message message = new Message("Some Image", "5551234567", Message.IS_SEND, Message.PICTURE, 0);
                    ArrayList<Packet> sentImagePackets = Packet.encodeImage(message, os.toByteArray());
                    os.close();
                    for (Packet p : sentImagePackets) {
                        bs.storeImagePacket(p);
                        ArrayList<Packet> image = bs.detectImageReceived();
                        if (image != null) {
                            System.out.println("Image detected");
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            for (Packet p2 : image) {
                                outputStream.write(p2.getContent());
                            }

                            Bitmap bitmap2 = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().length);
                            String contentType2 = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(outputStream.toByteArray()));
                            System.out.println(contentType2);
                        }
                    }
                } catch(IOException ex) {

                }
            }
        });
    }

    private void clearImages() {
        mPictures.clear();

        LinearLayout layout = findViewById(R.id.layout_picture);
        layout.setVisibility(View.GONE);
        TextView pictureText = findViewById(R.id.textview_picture);
        pictureText.setText("");
    }

    private Point getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        return displaySize;
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

    private boolean ReceiveMessage(final Message msg) {
        Log.d(TAG, "Message received: " + msg.getBody() + " from user: " + msg.getContactId());
        int currentSize = mAdapter.getItemCount();

        mAdapter.addMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

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
                String filePath = getPath(contentURI);
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
            }
        }
    }

    // TODO use non-deprecated function
    private String getPath(Uri uri) {
        if( uri == null ) {
            Log.e(TAG, "Null URI");
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }

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

                if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && msg.getDataType() == Message.PICTURE) {
                    msg.setBody("Error: Unable to read image. Access to storage is required.");
                    msg.setDataType(Message.TEXT);
                }

                mAdapter.addMessage(msg);
                mAdapter.notifyItemInserted(currentSize);

                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }
}
