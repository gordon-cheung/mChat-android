package com.example.macbook.mchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.view.LayoutInflater;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ChatAdapter extends RecyclerView.Adapter {
    private final static int IS_SEND_TEXT = 1;
    private final static int IS_RECEIVE_TEXT = 2;
    private final static int IS_SEND_PICTURE = 3;
    private final static int IS_RECEIVE_PICTURE = 4;

    private String TAG = ChatActivity.class.getSimpleName();
    private ArrayList<Message> mMessageList;

    private int mDisplayHeight;
    private int mDisplayWidth;

    public ChatAdapter(int displayWidth, int displayHeight) {
        this.mMessageList = new ArrayList<Message>();
        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;
    }

    public void addMessage(Message message) {
        mMessageList.add(message);
    }

    public int updateMessage(Message updateMessage) {
        for (int i = 0; i < mMessageList.size(); i++) {
            Message msg = mMessageList.get(i);
            if (msg.getMsgAckId() == updateMessage.getMsgId()) {
                msg.setStatus(Message.STATUS_SENT);
                return i;
            }
        }

        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);
        if (message.getDataType() == Message.PICTURE) {
            if (message.getType() == Message.IS_SEND) {
                return IS_SEND_PICTURE;
            }
            else {
                return IS_RECEIVE_PICTURE;
            }
        }
        else {
            if (message.getType() == Message.IS_SEND) {
                return IS_SEND_TEXT;
            }
            else {
                return IS_RECEIVE_TEXT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IS_SEND_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent_listitem, parent, false);
            SentTextMessageTextViewHolder holder = new SentTextMessageTextViewHolder(view);
            return holder;
        } else if (viewType == IS_RECEIVE_TEXT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received_listitem, parent, false);
            ReceivedTextMessageViewHolder holder = new ReceivedTextMessageViewHolder(view);
            return holder;
        } else if (viewType == IS_SEND_PICTURE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_picture_sent_listitem, parent, false);
            SentPictureMessageViewHolder holder = new SentPictureMessageViewHolder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_picture_received_listitem, parent, false);
            ReceivedPictureMessageViewHolder holder = new ReceivedPictureMessageViewHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        Message currentMessage = mMessageList.get(position);

        if (holder.getItemViewType() == IS_SEND_TEXT) {
            ((SentTextMessageTextViewHolder) holder).bind(currentMessage);
        }
        else if (holder.getItemViewType() == IS_RECEIVE_TEXT) {
            ((ReceivedTextMessageViewHolder) holder).bind(currentMessage);
        }
        else if (holder.getItemViewType() == IS_SEND_PICTURE) {
            ((SentPictureMessageViewHolder) holder).bind(currentMessage);
        }
        else  {
            ((ReceivedPictureMessageViewHolder) holder).bind(currentMessage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class SentTextMessageTextViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessageBody;
        public TextView mMessageTimestamp;
        public ImageView mMessageStatus;

        public SentTextMessageTextViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.text_message_body);
            mMessageTimestamp = itemView.findViewById(R.id.message_timestamp);
            mMessageStatus = itemView.findViewById(R.id.message_status);

        }

        void bind(Message message) {
            mMessageBody.setText(message.getBody());
            mMessageTimestamp.setText(DateUtilities.getDateString(message.getTimestamp()));
            mMessageStatus.setImageResource(getMessageStatusIcon(message.getStatus()));
        }
    }

    public class SentPictureMessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView mMessageBody;
        public TextView mMessageTimestamp;
        public ImageView mMessageStatus;

        public SentPictureMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.picture_message_body);
            mMessageTimestamp = itemView.findViewById(R.id.message_timestamp);
            mMessageStatus = itemView.findViewById(R.id.message_status);
        }

        void bind(final Message message) {
            try {
                loadImage(mMessageBody, message.getBody());
                mMessageTimestamp.setText(DateUtilities.getDateString(message.getTimestamp()));
                mMessageStatus.setImageResource(getMessageStatusIcon(message.getStatus()));
                mMessageBody.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(MChatApplication.getAppContext(), FullImageViewActivity.class);
                        intent.putExtra(AppNotification.IMAGE_DATA, message.getBody());
                        MChatApplication.getAppContext().startActivity(intent);
                    }
                });
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    public class ReceivedTextMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessageBody;
        public TextView mMessageTimestamp;

        public ReceivedTextMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.text_message_body);
            mMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp);
        }

        void bind(Message message) {

            mMessageBody.setText(message.getBody());
            mMessageTimestamp.setText(DateUtilities.getDateString(message.getTimestamp()));
        }
    }

    public class ReceivedPictureMessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView mMessageBody;
        public TextView mMessageTimestamp;

        public ReceivedPictureMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.picture_message_body);
            mMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp);
        }

        void bind(final Message message) {
            try {
                loadImage(mMessageBody, message.getBody());
                mMessageTimestamp.setText(DateUtilities.getDateString(message.getTimestamp()));

                mMessageBody.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(MChatApplication.getAppContext(), FullImageViewActivity.class);
                        intent.putExtra(AppNotification.IMAGE_DATA, message.getBody());
                        MChatApplication.getAppContext().startActivity(intent);
                    }
                });
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    private void loadImage(ImageView view, String filePath) throws IOException {
        try {
            Uri uri = Uri.fromFile(new File(filePath));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(MChatApplication.getAppContext().getContentResolver(), uri);
            Point size = getThumbnailSize(bitmap, mDisplayWidth / 2, mDisplayHeight / 2);

            Glide.with(MChatApplication.getAppContext()).load(bitmap).apply(new RequestOptions().override(size.x, size.y)).into(view);
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            throw ex;
        }
    }

    private Point getThumbnailSize(Bitmap image, float maxWidth, float maxHeight) {
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
        return new Point((int)width, (int)height);
    }

    private int getMessageStatusIcon(int status) {
        if (status == Message.STATUS_PENDING) {
            return R.drawable.ic_message_pending;
        } else if (status == Message.STATUS_SENT) {
            return R.drawable.ic_message_sent;
        } else if (status == Message.STATUS_RECEIVED) {
            return R.drawable.ic_message_received;
        } else {
            return R.drawable.ic_message_fail;
        }
    }

}
