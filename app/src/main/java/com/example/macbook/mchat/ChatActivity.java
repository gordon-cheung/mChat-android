package com.example.macbook.mchat;

import android.Manifest;
import android.content.*;
import android.database.Cursor;
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
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends MChatActivity {
    private String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;

    private Contact mContact;
    private String contactId;
    private ArrayList<String> mPictures = new ArrayList<String>();

    private final static int GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar((Toolbar) findViewById(R.id.app_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContact = (Contact)getIntent().getExtras().getSerializable(AppNotification.CONTACT_DATA);
        contactId = mContact.getPhoneNumber();
        getSupportActionBar().setTitle(mContact.getName());

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
                    final Message msg = new Message(imageFilePath, contactId, Message.IS_SEND, Message.PICTURE, MChatApplication.getAppMsgId());
                    sendMessage(msg);
                }

                clearImages();

                if (!message.isEmpty()) {
                    sendMessage(new Message(message, contactId, Message.IS_SEND, Message.TEXT, MChatApplication.getAppMsgId()));
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

        // TODO: REMOVE
        final Button testButton = findViewById(R.id.button_chatbox_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "TEST BUTTON CLICKED");

                Message updateMessage = new Message("", "5878880963", Message.IS_SEND, Message.SENT, 4);
                updateMessage.setMsgAckId(-1);
                Intent intent = new Intent(AppNotification.ACK_RECEIVED_NOTIFICATION);
                intent.putExtra(AppNotification.ACK_RECEIVED_NOTIFICATION, updateMessage);
                sendBroadcast(intent);

//                try {
//                    BluetoothService bs = new BluetoothService();
//                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_image1);
//                    int size = bitmap.getByteCount();
//                    ByteArrayOutputStream os = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, os);
//                    int compressedSize = os.toByteArray().length;
//
//                    Message message = new Message("Some Image", "4038092883", Message.IS_SEND, Message.PICTURE, 0);
//                    ArrayList<Packet> sentImagePackets = Packet.encodeImage(message, os.toByteArray());
//                    os.close();
//                    for (Packet p : sentImagePackets) {
//                        bs.storeImagePacket(p);
//                        ArrayList<Packet> image = bs.detectImageReceived();
//                        if (image != null) {
//                            System.out.println("Image detected");
//                            Bitmap receivedImageBitmap = bs.constructImage(image);
//                            Message receiveMessage = new Message(p, Message.IS_RECEIVE, Message.STATUS_RECEIVED);
//                            String url = bs.saveImage(bitmap, receiveMessage);
//
//                            receiveMessage.setDataType(Message.PICTURE);
//                            receiveMessage.setBody(url);
//
//                            final Intent intent = new Intent(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
//                            intent.putExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION, receiveMessage);
//                            sendBroadcast(intent);
//                        }
//                    }
//                } catch(IOException ex) {
//
//                }
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

    private void sendMessage(final Message msg) {
        int currentSize = mAdapter.getItemCount();

        if (mBluetoothService.isConnected()) {
            mAdapter.addMessage(msg);
            mAdapter.notifyItemInserted(currentSize);
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

            final int msgAckId = mBluetoothService.send(msg);
            msg.setMsgAckId(msgAckId);
            if (msgAckId != -1) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Inserting stored sent message");
                        AppDatabase.getInstance().messageDao().insert(msg);
                    }
                });
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "MChat is currently not connected, message will not send", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean receiveMessage(final Message msg) {
        Log.d(TAG, "Message received: " + msg.getBody() + " from user: " + msg.getContactId());
        int currentSize = mAdapter.getItemCount();

        mAdapter.addMessage(msg);
        mAdapter.notifyItemInserted(currentSize);

        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        return true;
    }

    private boolean updateMessageStatus(final Message msg) {
        int itemChangePosition = mAdapter.updateMessage(msg);
        mAdapter.notifyItemChanged(itemChangePosition);

        return true;
    }

    @Override
    public void onAppNotificationReceived(Intent intent) {
        final String action = intent.getAction();
        if (action == AppNotification.MESSAGE_RECEIVED_NOTIFICATION) {
            Log.d(TAG, "Message Received");
            Message msg = (Message)intent.getSerializableExtra(AppNotification.MESSAGE_RECEIVED_NOTIFICATION);
            if (msg.getContactId().equals(contactId)) {
                receiveMessage(msg);
            }
        }
        else if (action == AppNotification.ACK_RECEIVED_NOTIFICATION) {
            Log.d(TAG, "ACK Received");
            Message msg = (Message)intent.getSerializableExtra(AppNotification.ACK_RECEIVED_NOTIFICATION);
            if (msg.getContactId().equals(contactId)) {
                updateMessageStatus(msg);
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
