package com.helpmethen.contacts.model

data class ContactDetails(
    val id: Long,
    val name: String,
    val phones: List<String>,
    val emails: List<String>
)