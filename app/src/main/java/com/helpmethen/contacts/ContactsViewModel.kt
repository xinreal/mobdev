package com.helpmethen.contacts

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.helpmethen.contacts.model.ContactDetails
import com.helpmethen.contacts.model.ContactSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsRepository(application)

    var contacts by mutableStateOf<List<ContactSummary>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var selectedContact by mutableStateOf<ContactDetails?>(null)
        private set

    private var loadedOnce = false

    fun loadContacts(force: Boolean = false) {
        if (loadedOnce && !force) return

        viewModelScope.launch {
            isLoading = true
            contacts = withContext(Dispatchers.IO) {
                repository.loadContactNames()
            }
            isLoading = false
            loadedOnce = true
        }
    }

    fun openContact(contact: ContactSummary) {
        viewModelScope.launch {
            selectedContact = withContext(Dispatchers.IO) {
                repository.loadContactDetails(contact.id, contact.name)
            }
        }
    }

    fun closeDialog() {
        selectedContact = null
    }
}