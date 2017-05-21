package com.example.alexey.mylauncher.recyclerview.items;

import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

public class Contact extends Item {
    private String phoneNumber;
    private BitmapDrawable photo;
    private String contactId;

    public Contact(@NonNull String name, @NonNull String phoneNumber, BitmapDrawable photo, @NonNull String contactId) {
        super(ItemType.CONTACT.getId(), name);
        this.phoneNumber = phoneNumber;
        this.photo = photo;
        this.contactId = contactId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public BitmapDrawable getPhoto() {
        return photo;
    }

    public void setPhoto(BitmapDrawable photo) {
        this.photo = photo;
    }

    public String getContactId() {
        return contactId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Contact && contactId.equals(((Contact) obj).contactId);
    }
}
