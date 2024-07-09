package com.example.gptvoiceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TextViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private List<String> dataSet = SingletonClass.getInstance().getChatList();

    @Override
    public int getItemViewType(int position) {
        return (position % 2 == 0) ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_message_card, parent, false);
            return new UserChatViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ai_msg_card, parent, false);
            return new AIChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (dataSet != null && !dataSet.isEmpty()) {
            String item = dataSet.get(position);
            if (getItemViewType(position) == VIEW_TYPE_USER) {
                ((UserChatViewHolder) holder).textView.setText(item);
            } else {
                ((AIChatViewHolder) holder).textView.setText(item);
            }
        }
    }

    @Override
    public int getItemCount() {
        dataSet = SingletonClass.getInstance().getChatList();
        if (dataSet != null && !dataSet.isEmpty()) {
            return dataSet.size();
        } else {
            return 0;
        }
    }

    public static class UserChatViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public UserChatViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.userTextView);
        }
    }

    public static class AIChatViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public AIChatViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.aiTextView);
        }
    }
}