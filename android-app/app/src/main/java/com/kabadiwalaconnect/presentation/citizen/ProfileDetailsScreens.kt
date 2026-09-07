package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kabadiwalaconnect.ui.components.AppTopBar
import com.kabadiwalaconnect.ui.theme.Cream

@Composable
fun PersonalInformationScreen(nav: NavHostController) {

    var name by remember { mutableStateOf("Sahil") }
    var phone by remember { mutableStateOf("+91 XXXXX XXXXX") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AppTopBar(nav, "Personal Information")

        Spacer(Modifier.height(10.dp))

        Text(
            "Manage your personal details",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}


@Composable
fun SavedAddressesScreen(nav: NavHostController) {

    var address by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AppTopBar(nav, "Saved Addresses")

        Text(
            "Save your frequently used pickup addresses",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("New Address") },
            placeholder = { Text("Enter your address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = {
                // Address saving can be connected to backend later
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Address")
        }

        if (address.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = address,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun RewardsScreen(nav: NavHostController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        AppTopBar(nav, "Rewards & Impact")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Your Recycling Impact",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(16.dp))

                Text("♻️ Total recycled: 0 kg")

                Spacer(Modifier.height(8.dp))

                Text("🌱 CO₂ saved: 0 kg")

                Spacer(Modifier.height(8.dp))

                Text("⭐ Reward points: 0")
            }
        }

        Text(
            "Complete more recycling pickups to increase your environmental impact and earn rewards!",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}