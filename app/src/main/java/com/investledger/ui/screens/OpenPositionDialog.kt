     1|package com.investledger.ui.screens
     2|
     3|import androidx.compose.foundation.clickable
     4|import androidx.compose.foundation.layout.*
     5|import androidx.compose.foundation.lazy.LazyColumn
     6|import androidx.compose.foundation.lazy.items
     7|import androidx.compose.foundation.text.KeyboardOptions
     8|import androidx.compose.material.icons.Icons
     9|import androidx.compose.material.icons.filled.Close
    10|import androidx.compose.material3.*
    11|import androidx.compose.runtime.*
    12|import androidx.compose.ui.Alignment
    13|import androidx.compose.ui.Modifier
    14|import androidx.compose.ui.focus.FocusManager
    15|import androidx.compose.ui.platform.LocalFocusManager
    16|import androidx.compose.ui.text.font.FontWeight
    17|import androidx.compose.ui.text.input.KeyboardType
    18|import androidx.compose.ui.unit.dp
    19|import com.investledger.data.NameTypePair
import com.investledger.data.Position
    20|import com.investledger.ui.components.DateTimePicker
    21|import com.investledger.ui.theme.*
    22|import com.investledger.viewmodel.InvestViewModel
    23|import kotlinx.coroutines.Job
    24|import kotlinx.coroutines.delay
    25|import kotlinx.coroutines.launch
    26|
    27|/**
    28| * 建仓对话框 - 支持两种计算方式、日期时间设置和自动补全
    29| * 1. 按成本价+数量
    30| * 2. 按金额+数量（自动计算成本价）
    31| */
    32|@OptIn(ExperimentalMaterial3Api::class)
    33|@Composable
    34|fun OpenPositionDialog(
    35|    viewModel: InvestViewModel,
    36|    onDismiss: () -> Unit,
    37|    onConfirm: (name: String, type: String, costPrice: Double, quantity: Double, note: String, createdAt: Long, mergeWithExisting: Boolean) -> Unit
    38|) {
    39|    var name by remember { mutableStateOf("") }
    40|    var type by remember { mutableStateOf("股票") }
    41|    var note by remember { mutableStateOf("") }
    42|    var expanded by remember { mutableStateOf(false) }
    43|    
    44|    // 自动补全相关状态
    45|    var nameSuggestions by remember { mutableStateOf<List<NameTypePair>>(emptyList()) }
    46|    var showSuggestions by remember { mutableStateOf(false) }
    47|    var searchJob by remember { mutableStateOf<Job?>(null) }
    48|    val coroutineScope = rememberCoroutineScope()
    49|    val focusManager = LocalFocusManager.current
    
    // 加仓确认状态
    var showMergeDialog by remember { mutableStateOf(false) }
    var existingPositionForMerge by remember { mutableStateOf<Position?>(null) }
    50|    
    51|    // 输入模式：0=成本价+数量, 1=金额+数量
    52|    var inputMode by remember { mutableStateOf(0) }
    53|    
    54|    // 模式1：成本价和数量
    55|    var costPrice by remember { mutableStateOf("") }
    56|    var quantityMode1 by remember { mutableStateOf("") }
    57|    
    58|    // 模式2：金额和数量
    59|    var totalAmount by remember { mutableStateOf("") }
    60|    var quantityMode2 by remember { mutableStateOf("") }
    61|    
    62|    // 日期时间
    63|    var createdAt by remember { mutableStateOf(System.currentTimeMillis()) }
    64|    
    65|    val types = listOf("股票", "基金", "加密货币", "债券", "其他")
    66|    
    67|    // 计算实时总额显示
    68|    val displayTotal = when (inputMode) {
    69|        0 -> {
    70|            val cost = costPrice.toDoubleOrNull() ?: 0.0
    71|            val qty = quantityMode1.toDoubleOrNull() ?: 0.0
    72|            if (cost > 0 && qty > 0) cost * qty else 0.0
    73|        }
    74|        1 -> {
    75|            totalAmount.toDoubleOrNull() ?: 0.0
    76|        }
    77|        else -> 0.0
    78|    }
    79|    
    80|    // 计算实际成本价（用于保存）
    81|    val finalCostPrice = when (inputMode) {
    82|        0 -> costPrice.toDoubleOrNull() ?: 0.0
    83|        1 -> {
    84|            val amount = totalAmount.toDoubleOrNull() ?: 0.0
    85|            val qty = quantityMode2.toDoubleOrNull() ?: 0.0
    86|            if (qty > 0) amount / qty else 0.0
    87|        }
    88|        else -> 0.0
    89|    }
    90|    
    91|    // 计算实际数量
    92|    val finalQuantity = when (inputMode) {
    93|        0 -> quantityMode1.toDoubleOrNull() ?: 0.0
    94|        1 -> quantityMode2.toDoubleOrNull() ?: 0.0
    95|        else -> 0.0
    96|    }
    97|    
    98|    // 更新建议列表（带防抖和协程取消）
    99|    fun updateSuggestions(query: String) {
   100|        searchJob?.cancel()  // 取消上一次的查询
   101|        searchJob = coroutineScope.launch {
   102|            delay(300)  // 300ms 防抖，避免频繁查询
   103|            nameSuggestions = viewModel.getNameSuggestions(query)
   104|            showSuggestions = nameSuggestions.isNotEmpty()
   105|        }
   106|    }
   107|    
   108|    // 选择建议项
   109|    fun selectSuggestion(suggestion: NameTypePair) {
   110|        name = suggestion.name
   111|        type = suggestion.type
   112|        showSuggestions = false
   113|        focusManager.clearFocus()  // 收起键盘
   114|    }
   115|    
   116|    AlertDialog(
   117|        onDismissRequest = onDismiss,
   118|        title = { 
   119|            Text(
   120|                "建仓",
   121|                style = MaterialTheme.typography.headlineSmall
   122|            )
   123|        },
   124|        text = {
   125|            Column(
   126|                modifier = Modifier.fillMaxWidth(),
   127|                verticalArrangement = Arrangement.spacedBy(12.dp)
   128|            ) {
   129|                // 名称输入（带自动补全）
   130|                Box(modifier = Modifier.fillMaxWidth()) {
   131|                    Column {
   132|                        OutlinedTextField(
   133|                            value = name,
   134|                            onValueChange = { newName ->
   135|                                name = newName
   136|                                if (newName.isNotBlank()) {
   137|                                    updateSuggestions(newName)
   138|                                } else {
   139|                                    showSuggestions = false
   140|                                }
   141|                            },
   142|                            label = { Text("投资名称/代码") },
   143|                            modifier = Modifier.fillMaxWidth(),
   144|                            singleLine = true,
   145|                            colors = OutlinedTextFieldDefaults.colors(
   146|                                focusedBorderColor = GreenPrimary,
   147|                                unfocusedBorderColor = GrayBorder
   148|                            ),
   149|                            trailingIcon = if (name.isNotEmpty()) {
   150|                                {
   151|                                    IconButton(onClick = {
   152|                                        name = ""
   153|                                        showSuggestions = false
   154|                                    }) {
   155|                                        Icon(
   156|                                            imageVector = Icons.Default.Close,
   157|                                            contentDescription = "清空",
   158|                                            tint = GrayText
   159|                                        )
   160|                                    }
   161|                                }
   162|                            } else {
   163|                                null
   164|                            }
   165|                        )
   166|                        
   167|                        // 下拉建议列表
   168|                        if (showSuggestions && nameSuggestions.isNotEmpty()) {
   169|                            Card(
   170|                                modifier = Modifier
   171|                                    .fillMaxWidth()
   172|                                    .padding(top = 4.dp),
   173|                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
   174|                            ) {
   175|                                LazyColumn(
   176|                                    modifier = Modifier
   177|                                        .fillMaxWidth()
   178|                                        .heightIn(max = 200.dp)
   179|                                ) {
   180|                                    items(nameSuggestions) { suggestion ->
   181|                                        SuggestionItem(
   182|                                            suggestion = suggestion,
   183|                                            onClick = { selectSuggestion(suggestion) }
   184|                                        )
   185|                                    }
   186|                                }
   187|                            }
   188|                        }
   189|                    }
   190|                }
   191|                
   192|                // 类型选择
   193|                ExposedDropdownMenuBox(
   194|                    expanded = expanded,
   195|                    onExpandedChange = { expanded = !expanded }
   196|                ) {
   197|                    OutlinedTextField(
   198|                        value = type,
   199|                        onValueChange = {},
   200|                        readOnly = true,
   201|                        label = { Text("投资类型") },
   202|                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
   203|                        modifier = Modifier.menuAnchor().fillMaxWidth(),
   204|                        colors = OutlinedTextFieldDefaults.colors(
   205|                            focusedBorderColor = GreenPrimary,
   206|                            unfocusedBorderColor = GrayBorder
   207|                        )
   208|                    )
   209|                    
   210|                    ExposedDropdownMenu(
   211|                        expanded = expanded,
   212|                        onDismissRequest = { expanded = false }
   213|                    ) {
   214|                        types.forEach { selection ->
   215|                            DropdownMenuItem(
   216|                                text = { Text(selection) },
   217|                                onClick = {
   218|                                    type = selection
   219|                                    expanded = false
   220|                                }
   221|                            )
   222|                        }
   223|                    }
   224|                }
   225|                
   226|                // 日期时间选择
   227|                DateTimePicker(
   228|                    timestamp = createdAt,
   229|                    onTimestampChange = { createdAt = it },
   230|                    label = "购买日期"
   231|                )
   232|                
   233|                // 输入模式选择
   234|                SingleChoiceSegmentedButtonRow(
   235|                    modifier = Modifier.fillMaxWidth()
   236|                ) {
   237|                    SegmentedButton(
   238|                        selected = inputMode == 0,
   239|                        onClick = { inputMode = 0 },
   240|                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
   241|                    ) {
   242|                        Text("成本价+数量")
   243|                    }
   244|                    SegmentedButton(
   245|                        selected = inputMode == 1,
   246|                        onClick = { inputMode = 1 },
   247|                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
   248|                    ) {
   249|                        Text("金额+数量")
   250|                    }
   251|                }
   252|                
   253|                // 根据模式显示不同的输入
   254|                when (inputMode) {
   255|                    0 -> {
   256|                        // 模式1：成本价 + 数量
   257|                        OutlinedTextField(
   258|                            value = costPrice,
   259|                            onValueChange = { 
   260|                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
   261|                                    costPrice = it
   262|                                }
   263|                            },
   264|                            label = { Text("成本价") },
   265|                            modifier = Modifier.fillMaxWidth(),
   266|                            singleLine = true,
   267|                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
   268|                            colors = OutlinedTextFieldDefaults.colors(
   269|                                focusedBorderColor = GreenPrimary,
   270|                                unfocusedBorderColor = GrayBorder
   271|                            )
   272|                        )
   273|                        
   274|                        OutlinedTextField(
   275|                            value = quantityMode1,
   276|                            onValueChange = { 
   277|                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
   278|                                    quantityMode1 = it
   279|                                }
   280|                            },
   281|                            label = { Text("数量") },
   282|                            modifier = Modifier.fillMaxWidth(),
   283|                            singleLine = true,
   284|                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
   285|                            colors = OutlinedTextFieldDefaults.colors(
   286|                                focusedBorderColor = GreenPrimary,
   287|                                unfocusedBorderColor = GrayBorder
   288|                            )
   289|                        )
   290|                    }
   291|                    1 -> {
   292|                        // 模式2：金额 + 数量
   293|                        OutlinedTextField(
   294|                            value = totalAmount,
   295|                            onValueChange = { 
   296|                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
   297|                                    totalAmount = it
   298|                                }
   299|                            },
   300|                            label = { Text("金额") },
   301|                            modifier = Modifier.fillMaxWidth(),
   302|                            singleLine = true,
   303|                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
   304|                            colors = OutlinedTextFieldDefaults.colors(
   305|                                focusedBorderColor = GreenPrimary,
   306|                                unfocusedBorderColor = GrayBorder
   307|                            )
   308|                        )
   309|                        
   310|                        OutlinedTextField(
   311|                            value = quantityMode2,
   312|                            onValueChange = { 
   313|                                if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) {
   314|                                    quantityMode2 = it
   315|                                }
   316|                            },
   317|                            label = { Text("数量") },
   318|                            modifier = Modifier.fillMaxWidth(),
   319|                            singleLine = true,
   320|                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
   321|                            colors = OutlinedTextFieldDefaults.colors(
   322|                                focusedBorderColor = GreenPrimary,
   323|                                unfocusedBorderColor = GrayBorder
   324|                            )
   325|                        )
   326|                        
   327|                        // 显示自动计算的成本价
   328|                        if (finalCostPrice > 0) {
   329|                            Card(
   330|                                modifier = Modifier.fillMaxWidth(),
   331|                                colors = CardDefaults.cardColors(
   332|                                    containerColor = GreenLight
   333|                                ),
   334|                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
   335|                            ) {
   336|                                Row(
   337|                                    modifier = Modifier
   338|                                        .fillMaxWidth()
   339|                                        .padding(12.dp),
   340|                                    horizontalArrangement = Arrangement.SpaceBetween,
   341|                                    verticalAlignment = Alignment.CenterVertically
   342|                                ) {
   343|                                    Text(
   344|                                        "自动计算成本价",
   345|                                        style = MaterialTheme.typography.bodySmall,
   346|                                        color = GrayText
   347|                                    )
   348|                                    Text(
   349|                                        String.format("%.4f", finalCostPrice),
   350|                                        style = MaterialTheme.typography.bodyMedium,
   351|                                        fontWeight = FontWeight.Bold,
   352|                                        color = GreenPrimary
   353|                                    )
   354|                                }
   355|                            }
   356|                        }
   357|                    }
   358|                }
   359|                
   360|                // 实时显示总金额
   361|                if (displayTotal > 0) {
   362|                    Card(
   363|                        modifier = Modifier.fillMaxWidth(),
   364|                        colors = CardDefaults.cardColors(
   365|                            containerColor = MaterialTheme.colorScheme.primaryContainer
   366|                        ),
   367|                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
   368|                    ) {
   369|                        Column(
   370|                            modifier = Modifier
   371|                                .fillMaxWidth()
   372|                                .padding(12.dp),
   373|                            horizontalAlignment = Alignment.CenterHorizontally
   374|                        ) {
   375|                            Text(
   376|                                "总金额",
   377|                                style = MaterialTheme.typography.labelSmall,
   378|                                color = MaterialTheme.colorScheme.onPrimaryContainer
   379|                            )
   380|                            Text(
   381|                                String.format("%.2f", displayTotal),
   382|                                style = MaterialTheme.typography.titleLarge,
   383|                                fontWeight = FontWeight.Bold,
   384|                                color = MaterialTheme.colorScheme.primary
   385|                            )
   386|                        }
   387|                    }
   388|                }
   389|                
   390|                // 备注（可选）
   391|                OutlinedTextField(
   392|                    value = note,
   393|                    onValueChange = { note = it },
   394|                    label = { Text("备注（可选）") },
   395|                    modifier = Modifier.fillMaxWidth(),
   396|                    colors = OutlinedTextFieldDefaults.colors(
   397|                        focusedBorderColor = GreenPrimary,
   398|                        unfocusedBorderColor = GrayBorder
   399|                    )
   400|                )
   401|            }
   402|        },
   403|        confirmButton = {
   404|            TextButton(
   405|                onClick = {
   406|                    if (name.isNotBlank() && finalCostPrice > 0 && finalQuantity > 0) {
   407|                        onConfirm(name, type, finalCostPrice, finalQuantity, note, createdAt)
   408|                    }
   409|                },
   410|                enabled = name.isNotBlank() && finalCostPrice > 0 && finalQuantity > 0
   411|            ) {
   412|                Text("确定", color = GreenPrimary)
   413|            }
   414|        },
   415|        dismissButton = {
   416|            TextButton(onClick = onDismiss) {
   417|                Text("取消")
   418|            }
   419|        }
   420|    )
   421|}
   422|
   423|/**
   424| * 建议项组件
   425| */
   426|@Composable
   427|private fun SuggestionItem(
   428|    suggestion: NameTypePair,
   429|    onClick: () -> Unit
   430|) {
   431|    Row(
   432|        modifier = Modifier
   433|            .fillMaxWidth()
   434|            .clickable(onClick = onClick)
   435|            .padding(horizontal = 16.dp, vertical = 12.dp),
   436|        horizontalArrangement = Arrangement.SpaceBetween,
   437|        verticalAlignment = Alignment.CenterVertically
   438|    ) {
   439|        Text(
   440|            text = suggestion.name,
   441|            style = MaterialTheme.typography.bodyMedium,
   442|            fontWeight = FontWeight.Medium
   443|        )
   444|        Text(
   445|            text = suggestion.type,
   446|            style = MaterialTheme.typography.labelSmall,
   447|            color = GrayText
   448|        )
   449|    }
   450|    HorizontalDivider(
   451|        modifier = Modifier.padding(horizontal = 16.dp),
   452|        thickness = 0.5.dp,
   453|        color = GrayBorder
   454|    )
   455|}
   456|