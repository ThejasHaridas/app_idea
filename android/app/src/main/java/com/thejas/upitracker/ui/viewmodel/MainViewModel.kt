package com.thejas.upitracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thejas.upitracker.data.db.AppDatabase
import com.thejas.upitracker.data.model.Category
import com.thejas.upitracker.data.model.SpendingLimit
import com.thejas.upitracker.data.model.Transaction
import com.thejas.upitracker.data.repository.TrackerRepository
import com.thejas.upitracker.utils.Classifier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CategorySummary(
    val category: Category,
    val spent: Double,
    val limit: Double?
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TrackerRepository(AppDatabase.getInstance(app))

    private val _month = MutableStateFlow(SimpleDateFormat("M", Locale.US).format(Date()))
    private val _year  = MutableStateFlow(SimpleDateFormat("yyyy", Locale.US).format(Date()))
    val month: StateFlow<String> = _month.asStateFlow()
    val year:  StateFlow<String> = _year.asStateFlow()

    val categories: StateFlow<List<Category>> = repo.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val limits: StateFlow<List<SpendingLimit>> = repo.getLimits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val transactions: StateFlow<List<Transaction>> =
        combine(_month, _year) { m, y -> m.padStart(2, '0') to y }
            .flatMapLatest { (m, y) -> repo.getTransactionsByMonth(m, y) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categorySummaries: StateFlow<List<CategorySummary>> =
        combine(categories, transactions, limits) { cats, txs, lims ->
            val limMap = lims.associate { it.categoryId to it.monthlyLimit }
            cats.map { cat ->
                val spent = txs.filter { it.categoryId == cat.id && it.type == "debit" }.sumOf { it.amount }
                CategorySummary(cat, spent, limMap[cat.id])
            }.sortedByDescending { it.spent }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setMonthYear(month: String, year: String) { _month.value = month; _year.value = year }

    fun addTransaction(tx: Transaction) = viewModelScope.launch {
        val cats = repo.getCategoriesOnce()
        val catId = tx.categoryId ?: Classifier.classify(tx.description, tx.vpa, cats)
        repo.addTransaction(tx.copy(categoryId = catId))
    }

    fun updateTransaction(tx: Transaction) = viewModelScope.launch { repo.updateTransaction(tx) }
    fun deleteTransaction(tx: Transaction) = viewModelScope.launch { repo.deleteTransaction(tx) }

    fun addCategory(cat: Category)  = viewModelScope.launch { repo.addCategory(cat) }
    fun deleteCategory(cat: Category) = viewModelScope.launch { repo.deleteCategory(cat) }

    fun setLimit(catId: Long, limit: Double) = viewModelScope.launch { repo.setLimit(catId, limit) }
    fun removeLimit(catId: Long) = viewModelScope.launch { repo.removeLimit(catId) }
}
