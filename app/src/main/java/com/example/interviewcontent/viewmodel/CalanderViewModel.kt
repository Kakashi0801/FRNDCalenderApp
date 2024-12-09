package com.example.interviewcontent.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewcontent.models.CalenderResponse
import com.example.interviewcontent.models.StatusResponse
import com.example.interviewcontent.models.Task
import com.example.interviewcontent.models.TaskDetail
import com.example.interviewcontent.repository.CalanderRepository
import com.example.interviewcontent.resources.Resources
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.DateFormatSymbols
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CalanderViewModel @Inject constructor(
    private val calanderRepository: CalanderRepository  // Inject the repository
) : ViewModel() {

    var mDaysList = MutableLiveData<List<String>>()
    var mUpdatedMonth = MutableLiveData<Int>()
    var mUpdatedYear = MutableLiveData<Int>()
    var mSelectedDate = MutableLiveData<String>()

    val taskDetail = TaskDetail("2024-01-08","Happy Birthday to me (Anshul)",1509,"Anshul's Birthday")


    var taskList : MutableLiveData<Resources<CalenderResponse>> = MutableLiveData()
    var calenderResponse:CalenderResponse? = null

    var submittedTask:MutableLiveData<Resources<StatusResponse>> = MutableLiveData()
    var submittedTaskResponse:StatusResponse? = null

    var messageDeletion: MutableLiveData<Resources<String>> = MutableLiveData()
    var deletedTask: Task? = null
    var messageResponse:String? = null

    var allTasksData:MutableLiveData<ArrayList<Task>> = MutableLiveData()

    var dbTaskData :ArrayList<Task>? = null

    var submittedTaskObj:Task? = null




    init {
    }
    fun addDailyTask(date: String, title: String?) {
        // Implement functionality to add a task
    }

    fun generateDaysForMonth(selectedYear: Int, selectedMonth: Int){
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val daysList = mutableListOf<String>()
        daysList.addAll(daysOfWeek)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        for (i in 1 until firstDayOfWeek) {
            daysList.add("")
        }

        for (day in 1..daysInMonth) {
            daysList.add(day.toString())
        }

        mDaysList.value = daysList
    }

    fun changeTheCalenderView(changedBackwards: Boolean, selectedYear: Int, selectedMonth: Int) {
        val updatedMonth: Int
        val updatedYear: Int

        if (changedBackwards) {
            if (selectedMonth == Calendar.JANUARY) {
                updatedMonth = Calendar.DECEMBER
                updatedYear = selectedYear -1
            }else{
                updatedMonth = selectedMonth - 1
                updatedYear = selectedYear
            }
        }else{
            if(selectedMonth == Calendar.DECEMBER ){
                updatedMonth = Calendar.JANUARY
                updatedYear = selectedYear +1
            }else{
                updatedMonth = selectedMonth+1
                updatedYear = selectedYear
            }
        }
        mUpdatedMonth.value = updatedMonth
        mUpdatedYear.value = updatedYear
       generateDaysForMonth(updatedYear,updatedMonth)
    }

    fun getMonthName(monthIndex: Int): String {
        val monthNames = DateFormatSymbols().months
        return if (monthIndex in 0..11) monthNames[monthIndex] else "Invalid Month"
    }


    fun getTaskList(userId:Int)= viewModelScope.launch{
        taskList.postValue(Resources.Loading())
        val response = calanderRepository.fetchCalendarTaskList(123)
        taskList.postValue(handleCalenderResponse(response))
    }

    fun submitDailyTask(userId: Int,taskDetail: TaskDetail)=viewModelScope.launch {
        submittedTask.postValue(Resources.Loading())
        val response = calanderRepository.submitDailyTask(123,taskDetail)
        submittedTask.postValue(handleTaskSubmitResponse(response))
    }

    fun deleteDailyTask(userId: Int,taskId: Int)=viewModelScope.launch {
        messageDeletion.postValue(Resources.Loading())
        val response = calanderRepository.deleteDailyTask(123,taskId)
        messageDeletion.postValue(handleTaskDeleteResponse(response))
    }

    private fun handleTaskDeleteResponse(response: Response<StatusResponse>): Resources<String>? {
        if (response.isSuccessful){
            response.body().let {ressult->
                return Resources.Success((messageResponse ?: ressult)?.toString())!!
            }
        }
        return Resources.Error(response.message(),data = null)
    }


    private fun handleCalenderResponse(response: Response<CalenderResponse>):Resources<CalenderResponse>{
        if (response.isSuccessful){
            response.body().let {ressult->
                return Resources.Success(calenderResponse?:ressult)!!
            }
        }
        return Resources.Error(response.message(),data = null)
    }


    private fun handleTaskSubmitResponse(response: Response<StatusResponse>):Resources<StatusResponse>{
        if (response.isSuccessful){
            response.body().let {ressult->
                return Resources.Success(submittedTaskResponse?:ressult)!!
            }
        }
        return Resources.Error(response.message(),data = null)
    }


    fun saveDailyTasks(taskList: List<Task>) = viewModelScope.launch{
        taskList.forEach {task->
            calanderRepository.upsertTaskFromDb(task)
        }
    }

    fun deleteDailyTask(task: Task) = viewModelScope.launch {
        calanderRepository.deleteTaskFromDb(task)
    }

    fun getAllTasks() = calanderRepository.getSavedArticlesFromDb()



    fun fetchTaskFromTheGivenDate(tasks: List<Task>, date: String){
        val taskList = ArrayList<Task>()

        for (task in tasks) {
            if (task.task_detail.date == date) {
                taskList.add(task)
            }
        }

        allTasksData.value = taskList
    }

    fun updateLists() {
        val originalList = allTasksData.value as ArrayList<Task>
        submittedTaskObj?.let { dbTaskData!!.add(it) }
        listOf(submittedTaskObj)?.let { saveDailyTasks(it as List<Task>) }
        originalList.add(submittedTaskObj!!)
        allTasksData.postValue(originalList)

    }

    fun removeListRecords(){
        val originalList = allTasksData.value
        deletedTask?.let { it1 -> originalList!!.remove(it1) }
        deletedTask?.let { it1 -> deleteDailyTask(it1) }
        deletedTask?.let { it1 -> dbTaskData!!.remove(it1) }

       allTasksData.value = originalList

    }
}