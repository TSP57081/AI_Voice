package com.example.gptvoiceapp;

import java.util.ArrayList;

public class SingletonClass {
    private static SingletonClass instance;
    private TextViewAdapter adapter;
    private ArrayList<String> chatList;

    private SingletonClass() {}

    public static SingletonClass getInstance() {
        if (instance == null) {
            instance = new SingletonClass();
        }
        return instance;
    }

    public TextViewAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(TextViewAdapter adapter) {
        this.adapter = adapter;
    }

    public ArrayList<String> getChatList() {
        return chatList;
    }

    public void setChatList(ArrayList<String> chatList) {
        this.chatList = chatList;
        adapter.notifyDataSetChanged();
    }
}