package org.osprey.trunkfriends.ui.authenticate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AuthState(
    server: String? = null
) {
    var skipDomain = server != null
    var activeStep by mutableStateOf("")
    var token  by mutableStateOf("")
    var domain by mutableStateOf(server ?: "")
    var url by mutableStateOf("")
    var clientSecret by mutableStateOf("")
    var clientId by mutableStateOf("")
    var code by mutableStateOf("")
}