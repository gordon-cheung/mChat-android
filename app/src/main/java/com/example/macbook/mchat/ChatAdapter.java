package com.example.macbook.mchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
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

        public SentTextMessageTextViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.text_message_body);
        }

        void bind(Message message) {
            mMessageBody.setText(message.getBody());
        }
    }

    public class SentPictureMessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView mMessageBody;

        public SentPictureMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.picture_message_body);
        }

        void bind(final Message message) {
            try {
                mMessageBody.setImageBitmap(getImage(message.getBody()));
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

        public ReceivedTextMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.text_message_body);
        }

        void bind(Message message) {
            mMessageBody.setText(message.getBody());
        }
    }

    public class ReceivedPictureMessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView mMessageBody;

        public ReceivedPictureMessageViewHolder(View itemView) {
            super(itemView);
            mMessageBody = itemView.findViewById(R.id.picture_message_body);
        }

        void bind(final Message message) {
            try {
                mMessageBody.setImageBitmap(getImage(message.getBody()));
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

    private Bitmap getImage(String filePath) throws IOException {
        try {
            Uri uri = Uri.fromFile(new File(filePath));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(MChatApplication.getAppContext().getContentResolver(), uri);
            bitmap = getThumbnail(bitmap, mDisplayWidth / 2, mDisplayHeight / 2);
            return bitmap;
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            throw ex;
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

}
