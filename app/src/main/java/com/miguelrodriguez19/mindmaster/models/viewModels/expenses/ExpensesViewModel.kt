package com.miguelrodriguez19.mindmaster.models.viewModels.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.miguelrodriguez19.mindmaster.models.structures.dto.MonthMovementsResponse

class ExpensesViewModel : ViewModel() {
    private val _actualMonth = MutableLiveData<MonthMovementsResponse>()
    private val _allMonths = MutableLiveData<List<MonthMovementsResponse>>()
    val actualMonth: LiveData<MonthMovementsResponse> get() = _actualMonth
    val allMonths: LiveData<List<MonthMovementsResponse>> get() = _allMonths

    fun setActualMonth(movement: MonthMovementsResponse) {
        _actualMonth.value = movement
    }

    fun setAllMonths(movements: List<MonthMovementsResponse>) {
        _allMonths.value = movements
    }
}