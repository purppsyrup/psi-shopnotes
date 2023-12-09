package com.psi.shopnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen() {
    var newItemName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf(1) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val shoppingItemDao = remember { ShoppingListDatabase.getInstance(context).shoppingItemDao() }
    val items by shoppingItemDao.getAllItems().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Field untuk new item
        TextField(
            value = newItemName,
            onValueChange = { newItemName = it },
            label = { Text("Item Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    coroutineScope.launch {
                        addItemToShoppingList(newItemName, selectedDate, quantity, shoppingItemDao)
                    }
                    newItemName = ""
                    quantity = 1 // Reset quantity ke 1 setelah add item
                }
            )
        )

        // Display list
        LazyColumn {
            items(items) { item: ShoppingItem ->
                ShoppingItemRow(item = item) {
                    coroutineScope.launch {
                        onDeleteItem(item, shoppingItemDao)
                    }
                }
            }
        }

        // Select qty
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Quantity: $quantity", modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    if (quantity > 1) {
                        quantity--
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Quantity")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    quantity++
                }
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Increase Quantity")
            }
        }

        // Untuk set date
        CustomDatePicker(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Tombol add item
        Button(
            onClick = {
                coroutineScope.launch {
                    addItemToShoppingList(newItemName, selectedDate, quantity, shoppingItemDao)
                }
                newItemName = ""
                quantity = 1 // Reset quantity ke 1
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Add Item")
        }
    }
}

@Composable
fun ShoppingItemRow(item: ShoppingItem, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${item.name} - Quantity: ${item.quantity}")
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Date: ${item.date ?: "Not set"}")

        // Tombol delete
        IconButton(
            onClick = {
                onDeleteClick.invoke()
            }
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Item")
        }
    }
}

suspend fun addItemToShoppingList(
    itemName: String,
    date: String?,
    quantity: Int,
    shoppingItemDao: ShoppingItemDao
) {
    // Operasi async
    withContext(Dispatchers.IO) {
        shoppingItemDao.insert(ShoppingItem(name = itemName, date = date, quantity = quantity))
    }
}

suspend fun onDeleteItem(item: ShoppingItem, shoppingItemDao: ShoppingItemDao) {
    withContext(Dispatchers.IO) {
        shoppingItemDao.delete(item)
    }
}

@Composable
fun CustomDatePicker(
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDateState = remember { mutableStateOf(selectedDate) }

    Column(
        modifier = modifier
    ) {
        CustomDatePicker(
            selectedDate = selectedDateState.value,
            onDateSelected = { selectedDateState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Tombol set date
        Button(
            onClick = {
                onDateSelected(selectedDateState.value)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Set Date")
        }
    }
}

//fun scheduleNotification(itemName: String, date: String, context: Context) {
//    nanti
//}

