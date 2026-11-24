package com.example.busmanager.presentation.finance

import com.example.busmanager.domain.company.model.Company
import com.example.busmanager.domain.company.model.Money
import com.example.busmanager.domain.finance.model.FinancialTransaction
import com.example.busmanager.domain.finance.model.TransactionType
import com.example.busmanager.domain.fleet.model.Bus
import com.example.busmanager.domain.route.model.BusRoute
import kotlin.math.abs

enum class TransactionTypeFilter(val displayName: String) {
    ALL("Semua"),
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran")
}

data class FinanceUiState(
    val company: Company? = null,
    val transactions: List<FinancialTransaction> = emptyList(),
    val availableBuses: List<Bus> = emptyList(),
    val availableRoutes: List<BusRoute> = emptyList(),
    val selectedTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val selectedBusId: String? = null,
    val selectedRouteId: String? = null,
    val isLoading: Boolean = false
) {
    val filteredTransactions: List<FinancialTransaction>
        get() = transactions.filter { tx ->
            val matchType = when (selectedTypeFilter) {
                TransactionTypeFilter.ALL -> true
                TransactionTypeFilter.INCOME -> tx.type == TransactionType.INCOME
                TransactionTypeFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
            }
            val matchBus = selectedBusId == null || tx.busId == selectedBusId
            val matchRoute = selectedRouteId == null || tx.routeId == selectedRouteId
            matchType && matchBus && matchRoute
        }

    val totalIncome: Money
        get() {
            val sum = filteredTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount.amount }
            return Money.rupiah(sum)
        }

    val totalExpense: Money
        get() {
            val sum = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount.amount }
            return Money.rupiah(sum)
        }

    val netProfitAmount: Long
        get() = totalIncome.amount - totalExpense.amount

    val isProfitable: Boolean
        get() = netProfitAmount >= 0

    val netProfitFormatted: String
        get() {
            val formatted = Money.rupiah(abs(netProfitAmount)).formatRupiah()
            return if (isProfitable) "+$formatted" else "-$formatted"
        }

    val currentBalance: Money
        get() = company?.balance ?: Money.ZERO
}
