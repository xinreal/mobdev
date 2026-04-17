package com.helpmethen.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import com.helpmethen.contacts.model.ContactDetails
import com.helpmethen.contacts.model.ContactSummary
class ContactsRepository(private val context: Context) {

    @SuppressLint("Range")
    fun loadContactNames(): List<ContactSummary> {
        val result = mutableListOf<ContactSummary>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )

        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} IS NOT NULL",
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: continue
                result.add(ContactSummary(id = id, name = name))
            }
        }

        return result
    }

    @SuppressLint("Range")
    fun loadContactDetails(contactId: Long, contactName: String): ContactDetails {
        val phones = linkedSetOf<String>()
        val emails = linkedSetOf<String>()

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val phone = cursor.getString(phoneIndex)
                if (!phone.isNullOrBlank()) phones.add(phone)
            }
        }

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            while (cursor.moveToNext()) {
                val email = cursor.getString(emailIndex)
                if (!email.isNullOrBlank()) emails.add(email)
            }
        }

        return ContactDetails(
            id = contactId,
            name = contactName,
            phones = phones.toList(),
            emails = emails.toList()
        )
    }
}
