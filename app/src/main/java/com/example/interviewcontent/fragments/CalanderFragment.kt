package com.example.interviewcontent.fragments

import android.app.AlertDialog
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.interviewcontent.CalanderActivity
import com.example.interviewcontent.R
import com.example.interviewcontent.adapters.CustomCalenderViewAdapter
import com.example.interviewcontent.adapters.MonthViewAdapter
import com.example.interviewcontent.adapters.OnItemClick
import com.example.interviewcontent.adapters.onTaskDelete
import com.example.interviewcontent.databinding.FragmentCalanderBinding
import com.example.interviewcontent.models.Task
import com.example.interviewcontent.models.TaskDetail
import com.example.interviewcontent.resources.Resources
import com.example.interviewcontent.util.NetworkBroadCastReceiver
import com.example.interviewcontent.util.NetworkUtils
import com.example.interviewcontent.viewmodel.CalanderViewModel
import java.lang.ref.WeakReference
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class CalanderFragment : Fragment(R.layout.fragment_calander), View.OnClickListener, OnItemClick,onTaskDelete {

    lateinit var viewModel: CalanderViewModel
    lateinit var _binding: FragmentCalanderBinding
    private val binding get() = _binding
    private var selectedDate = ""
    private var taskToAdd = ""
    private var mSelectedYear = 2021
    private var mSelectedMonth = Calendar.DECEMBER
    var calenderSelectedDate: Int = -1
    var calenderSelectedMonth: Int = -1
    var calenderSelectedYear: Int = -1
    private val TAG = "CalanderFragment"
    private var globalTaskList = ArrayList<Task>()
    private var connectivityReceiver: NetworkBroadCastReceiver? = null
    private var alertDialog: AlertDialog? = null
    private var isToDelete = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calander, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as CalanderActivity).calanderViewModel
        initDateVariables()
        setClickListeners()
        setLiveDataObservers()
        setupSpinners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initDateVariables() {
        val currentDate = LocalDate.now()
        mSelectedYear = currentDate.year
        mSelectedMonth = currentDate.monthValue - 1
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        selectedDate = currentDate.format(formatter)
    }

    private fun setLiveDataObservers() {
        viewModel.mUpdatedMonth.observe(viewLifecycleOwner) {
            mSelectedMonth = it
            binding.monthLabel.setSelection(mSelectedMonth)
        }
        viewModel.mUpdatedYear.observe(viewLifecycleOwner) {
            val years = (1975..2075).toList()
            mSelectedYear = it
            binding.yearLabel.setSelection(years.indexOf(mSelectedYear))
        }
        viewModel.mDaysList.observe(viewLifecycleOwner) {
            binding.customCalendarView.layoutManager = GridLayoutManager(context, 7)
            binding.customCalendarView.adapter = CustomCalenderViewAdapter(it, WeakReference(this))
        }

        viewModel.taskList.observe(viewLifecycleOwner){response->
            when(response){
                is Resources.Error -> {
                   hideProgressBar()
                    if(response.message!=null){
                        Toast.makeText(activity,"Error occured", Toast.LENGTH_LONG)
                    }
                }
                is Resources.Loading -> { showProgressBar()}
                is Resources.Success -> {
                    hideProgressBar()
                    if(response.data !=null) {
                        globalTaskList = response.data.tasks as ArrayList<Task>
                        viewModel.saveDailyTasks(response.data.tasks)
                        viewModel.dbTaskData = response.data.tasks
                        viewModel.fetchTaskFromTheGivenDate(response.data.tasks,selectedDate)
                    }
                }
            }
        }

        viewModel.mSelectedDate.observe(viewLifecycleOwner){
            viewModel.dbTaskData?.let { it1 -> viewModel.fetchTaskFromTheGivenDate(it1,selectedDate) }
        }

        viewModel.getAllTasks().observe(viewLifecycleOwner){

            if(NetworkUtils.isInternetAvailable(requireContext())){
                viewModel.getTaskList(123)
            }else{
                if(it!=null && it.isNotEmpty()){
                    viewModel.dbTaskData = it as ArrayList<Task>?
                    viewModel.dbTaskData?.let { it1 -> viewModel.fetchTaskFromTheGivenDate(it1,selectedDate) }
                }else{
                    Toast.makeText(context,"Please check your internet connectivity to load data for the first time",Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.allTasksData.observe(viewLifecycleOwner){
            val monthViewAdapter = MonthViewAdapter(it,WeakReference(this),context,isToDelete)
            binding.dailyTaskRv.layoutManager = LinearLayoutManager(context)
            binding.dailyTaskRv.adapter = monthViewAdapter
        }

        viewModel.messageDeletion.observe(viewLifecycleOwner){
            if(it.data!=null){
                viewModel.removeListRecords()
            }
        }

        viewModel.submittedTask.observe(viewLifecycleOwner){
            if (it.data!=null){
                viewModel.updateLists()
            }

        }

    }

    private fun setClickListeners() {
        binding.addIcon.visibility = if(NetworkUtils.isInternetAvailable(requireContext())) View.VISIBLE else View.GONE
        binding.addIcon.setOnClickListener(this)
        binding.leftArrow.setOnClickListener(this)
        binding.rightArrow.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.add_icon-> {
                val taskBottomSheetDialog = TaskBottomSheetDialog(
                    requireContext(),
                    viewModel
                ) { task, title ->
                    Log.d(TAG, "onClick: {$task},{$title}")
                    val taskDetailObj = TaskDetail(
                         selectedDate, task, Math.random().toInt(),title
                    )
                    val taskobj = Task(taskDetailObj,Math.random().toInt())
                    Log.d("--------------", "onClick: {$taskobj}")
                    viewModel.submittedTaskObj = taskobj
                    viewModel.submitDailyTask(123,taskDetailObj)

                }
                taskBottomSheetDialog.show()
            }
            R.id.left_arrow -> viewModel.changeTheCalenderView(true, mSelectedYear, mSelectedMonth)
            R.id.right_arrow -> viewModel.changeTheCalenderView(false, mSelectedYear, mSelectedMonth)
        }
    }

    private fun setupSpinners() {
        val months = resources.getStringArray(R.array.months) // Define the months in strings.xml
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.monthLabel.adapter = monthAdapter

        val years = (1975..2075).toList()
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.yearLabel.adapter = yearAdapter

        binding.monthLabel.setSelection(mSelectedMonth)
        binding.yearLabel.setSelection(years.indexOf(mSelectedYear))

        viewModel.generateDaysForMonth(mSelectedYear, mSelectedMonth)

        binding.monthLabel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSelectedMonth = position
                updateSelectedDate(true,mSelectedMonth)
                viewModel.generateDaysForMonth(mSelectedYear, mSelectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.yearLabel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSelectedYear = years[position]
                updateSelectedDate(false, mSelectedYear)
                viewModel.generateDaysForMonth(mSelectedYear, mSelectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


    }

    private fun updateSelectedDate(isMonth: Boolean, substringInt: Int) {
        if (selectedDate.length != 10) {
            throw IllegalArgumentException("selectedDate must be in the format YYYY-MM-DD")
        }
        selectedDate = if (isMonth) {
            selectedDate.substring(0, 5) + substringInt.toString().padStart(2, '0') + selectedDate.substring(7)
        } else {
            substringInt.toString().padStart(4, '0') + selectedDate.substring(4)
        }
    }

    private fun hideProgressBar() {
        binding.loaderContainer.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.loaderContainer.visibility = View.VISIBLE
    }
    override fun didClickItem(date: String) {
        calenderSelectedDate = date.toIntOrNull() ?: -1
        calenderSelectedMonth = mSelectedMonth
        calenderSelectedYear = mSelectedYear
        binding.customCalendarView.adapter?.notifyDataSetChanged()
    }

    override fun isSelectedDate(date: String): Boolean {
        return mSelectedYear == calenderSelectedYear && mSelectedMonth == calenderSelectedMonth && date.toIntOrNull() == calenderSelectedDate
    }

    override fun selectedDate(date: String) {
        val day = date.toIntOrNull() ?: return

        val constructedDate = String.format(
            "%04d-%02d-%02d",
            mSelectedYear,
            mSelectedMonth + 1,
            day
        )

        selectedDate = constructedDate
        viewModel.mSelectedDate.value = constructedDate
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun isCurrentDate(date: String): Boolean {
        val currentDate = LocalDate.now()
        return currentDate.year == mSelectedYear && (currentDate.month.value - 1) == mSelectedMonth && currentDate.dayOfMonth == date.toIntOrNull()
    }

    override fun onTaskDelete(task: Task) {
        viewModel.deleteDailyTask(123,task.task_id)
        viewModel.deletedTask = task
    }


    override fun onResume() {
        super.onResume()
        connectivityReceiver = NetworkBroadCastReceiver { isConnected ->
            showConnectivityStatus(isConnected)
        }
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        requireContext().registerReceiver(connectivityReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        connectivityReceiver?.let { requireContext().unregisterReceiver(it) }
    }

    private fun showConnectivityStatus(isConnected: Boolean) {
        val statusMessage = if (isConnected) {
            "You are online."
        } else {
            "You are offline. You cannot add or delete a task"
        }

        if(!isConnected){
            binding.addIcon.isVisible = false
            isToDelete = false
        }else{
            binding.addIcon.isVisible = true
            isToDelete = true
        }
        // Show an AlertDialog with OK button
        alertDialog?.dismiss() // Dismiss any existing dialogs to avoid multiple popups
        alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Internet Status")
            .setMessage(statusMessage)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()

        alertDialog?.show()
    }
}
