package com.example.businessmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class Customer(
    val id: Int,
    val name: String,
    val phone: String,
    val balance: Double
)

data class InvoiceItem(
    val name: String,
    val qty: Int,
    val price: Double
) {
    val total: Double get() = qty * price
}

data class Invoice(
    val number: String,
    val customer: String,
    val date: String,
    val items: List<InvoiceItem>,
    val paid: Double
) {
    val total: Double get() = items.sumOf { it.total }
    val due: Double get() = total - paid
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BusinessManagerApp() }
    }
}

@Composable
fun BusinessManagerApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1565C0),
            secondary = Color(0xFF00897B)
        )
    ) {
        var tab by remember { mutableStateOf(0) }
        var showInvoice by remember { mutableStateOf(false) }
        var showPayment by remember { mutableStateOf(false) }

        val customers = remember {
            mutableStateListOf(
                Customer(1, "Rahul Sharma", "9876543210", 4200.0),
                Customer(2, "Neha Enterprises", "9812345678", 1750.0),
                Customer(3, "Amit Kumar", "9988776655", 0.0)
            )
        }
        val invoices = remember {
            mutableStateListOf(
                Invoice(
                    "INV-1001", "Rahul Sharma", "17 Aug 2026",
                    listOf(InvoiceItem("Product A", 2, 2500.0)),
                    800.0
                ),
                Invoice(
                    "INV-1002", "Neha Enterprises", "17 Aug 2026",
                    listOf(InvoiceItem("Product B", 3, 1750.0)),
                    5250.0
                )
            )
        }

        if (showInvoice) {
            InvoiceCreateScreen(
                customers = customers,
                onSave = { invoice ->
                    invoices.add(invoice)
                    showInvoice = false
                },
                onBack = { showInvoice = false }
            )
        } else if (showPayment) {
            PaymentScreen(
                customers = customers,
                onBack = { showPayment = false }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(
                            when (tab) {
                                0 -> "Business Manager"
                                1 -> "Sales & Billing"
                                2 -> "Customers"
                                else -> "Payments"
                            },
                            fontWeight = FontWeight.Bold
                        ) },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.NotificationsNone, "Notifications")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        listOf(
                            Icons.Default.Dashboard to "Home",
                            Icons.Default.ReceiptLong to "Sales",
                            Icons.Default.People to "Customers",
                            Icons.Default.Payments to "Payments"
                        ).forEachIndexed { i, pair ->
                            NavigationBarItem(
                                selected = tab == i,
                                onClick = { tab = i },
                                icon = { Icon(pair.first, pair.second) },
                                label = { Text(pair.second) }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(
                    Modifier.padding(padding).fillMaxSize()
                        .background(Color(0xFFF5F7FA))
                ) {
                    when (tab) {
                        0 -> DashboardScreen(invoices, customers)
                        1 -> SalesScreen(
                            invoices = invoices,
                            onCreate = { showInvoice = true }
                        )
                        2 -> CustomersScreen(customers)
                        3 -> PaymentsScreen(
                            customers = customers,
                            onRecordPayment = { showPayment = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(invoices: List<Invoice>, customers: List<Customer>) {
    val sales = invoices.sumOf { it.total }
    val due = invoices.sumOf { it.due }
    Column(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Good morning 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Business overview", color = Color.Gray)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Sales", "₹${"%,.0f".format(sales)}", Icons.Default.TrendingUp, Modifier.weight(1f))
            MetricCard("Receivable", "₹${"%,.0f".format(due)}", Icons.Default.ArrowDownward, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Invoices", invoices.size.toString(), Icons.Default.ReceiptLong, Modifier.weight(1f))
            MetricCard("Customers", customers.size.toString(), Icons.Default.People, Modifier.weight(1f))
        }
    }
}

@Composable
fun SalesScreen(invoices: List<Invoice>, onCreate: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Invoices", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("${invoices.size} invoices", color = Color.Gray)
            }
            Button(onClick = onCreate) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("New Invoice")
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(invoices) { invoice ->
                InvoiceCard(invoice)
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(invoice.number, fontWeight = FontWeight.Bold)
                Text("₹${"%,.2f".format(invoice.total)}", fontWeight = FontWeight.Bold)
            }
            Text(invoice.customer, color = Color.Gray)
            Text(invoice.date, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Paid: ₹${"%,.2f".format(invoice.paid)}")
                Text(
                    "Due: ₹${"%,.2f".format(invoice.due)}",
                    color = if (invoice.due > 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun InvoiceCreateScreen(
    customers: List<Customer>,
    onSave: (Invoice) -> Unit,
    onBack: () -> Unit
) {
    var customer by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<InvoiceItem>() }
    var error by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Customer", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    customer, { customer = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Customer name") },
                    singleLine = true
                )
            }
            item {
                Text("Add item", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    product, { product = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Product / service") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        qty, { qty = it.filter(Char::isDigit) },
                        Modifier.weight(1f),
                        label = { Text("Qty") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        price, { price = it },
                        Modifier.weight(1f),
                        label = { Text("Price") },
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val q = qty.toIntOrNull()
                        val p = price.toDoubleOrNull()
                        if (product.isNotBlank() && q != null && p != null && q > 0 && p >= 0) {
                            items.add(InvoiceItem(product.trim(), q, p))
                            product = ""; qty = "1"; price = ""
                        } else error = "Enter a product, quantity and valid price."
                    }
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add item")
                }
            }
            items(items) { item ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.name} × ${item.qty}")
                            Text("₹${"%,.2f".format(item.total)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                val total = items.sumOf { it.total }
                OutlinedTextField(
                    paid, { paid = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Amount paid") },
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                Text("Total: ₹${"%,.2f".format(total)}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Balance: ₹${"%,.2f".format(total - (paid.toDoubleOrNull() ?: 0.0))}",
                    color = MaterialTheme.colorScheme.error
                )
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(10.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val totalPaid = paid.toDoubleOrNull() ?: 0.0
                        if (customer.isBlank()) {
                            error = "Enter a customer name."
                        } else if (items.isEmpty()) {
                            error = "Add at least one item."
                        } else if (totalPaid < 0 || totalPaid > total) {
                            error = "Paid amount must be between ₹0 and the invoice total."
                        } else {
                            val number = "INV-" + (1000 + System.currentTimeMillis() % 9000)
                            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                            onSave(Invoice(number, customer.trim(), date, items.toList(), totalPaid))
                        }
                    }
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Save Invoice")
                }
            }
        }
    }
}

@Composable
fun CustomersScreen(customers: List<Customer>) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Customers", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(customers) { c ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(c.name, fontWeight = FontWeight.Bold)
                            Text(c.phone, color = Color.Gray)
                        }
                        Text(
                            "₹${"%,.2f".format(c.balance)} due",
                            color = if (c.balance > 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentsScreen(customers: List<Customer>, onRecordPayment: () -> Unit) {
    val totalDue = customers.sumOf { it.balance }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Payments", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Total receivable", color = Color.Gray)
                Text("₹${"%,.2f".format(totalDue)}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRecordPayment, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Record Payment")
        }
    }
}

@Composable
fun PaymentScreen(customers: List<Customer>, onBack: () -> Unit) {
    var selected by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Cash") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Customer", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                selected, { selected = it },
                Modifier.fillMaxWidth(),
                label = { Text("Customer name") },
                singleLine = true
            )
            OutlinedTextField(
                amount, { amount = it },
                Modifier.fillMaxWidth(),
                label = { Text("Payment amount") },
                singleLine = true
            )
            Text("Payment method")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cash", "UPI", "Bank", "Card").forEach {
                    FilterChip(
                        selected = method == it,
                        onClick = { method = it },
                        label = { Text(it) }
                    )
                }
            }
            Button(
                onClick = { saved = selected.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0 },
                Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(6.dp))
                Text("Save Payment")
            }
            if (saved) {
                Text("Payment recorded successfully ✓", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.Gray, fontSize = 13.sp)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
