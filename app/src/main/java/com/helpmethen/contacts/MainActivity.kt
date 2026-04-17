package com.helpmethen.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.helpmethen.contacts.model.ContactDetails
import com.helpmethen.contacts.model.ContactSummary

class MainActivity : ComponentActivity() {

    private val vm: ContactsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ContactsApp(vm = vm)
            }
        }
    }
}

@Composable
fun ContactsApp(vm: ContactsViewModel) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            vm.loadContacts()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            vm.loadContacts()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !hasPermission -> {
                    NoPermissionScreen(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    )
                }

                vm.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                vm.contacts.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_contacts),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    ContactsList(
                        contacts = vm.contacts,
                        onContactClick = vm::openContact
                    )
                }
            }

            vm.selectedContact?.let { details ->
                ContactDetailsDialog(
                    details = details,
                    onDismiss = vm::closeDialog
                )
            }
        }
    }
}

@Composable
fun NoPermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.permission_required_title))
        Text(text = stringResource(R.string.permission_required_message))
        Button(onClick = onRequestPermission) {
            Text(text = stringResource(R.string.grant_permission))
        }
    }
}

@Composable
fun ContactsList(
    contacts: List<ContactSummary>,
    onContactClick: (ContactSummary) -> Unit
) {
    LazyColumn {
        items(contacts, key = { it.id }) { contact ->
            ListItem(
                headlineContent = { Text(contact.name) },
                modifier = Modifier.clickable { onContactClick(contact) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun ContactDetailsDialog(
    details: ContactDetails,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(details.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.phones_title))
                if (details.phones.isEmpty()) {
                    Text(text = stringResource(R.string.no_phones))
                } else {
                    details.phones.forEach { Text(it) }
                }

                Text(text = stringResource(R.string.emails_title))
                if (details.emails.isEmpty()) {
                    Text(text = stringResource(R.string.no_emails))
                } else {
                    details.emails.forEach { Text(it) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close))
            }
        }
    )
}