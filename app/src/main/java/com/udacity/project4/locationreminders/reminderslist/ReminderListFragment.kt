package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationState
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    companion object {
        const val LOG_TAG: String = "ReminderListFragment"
        const val LOG_TAG_TEST: String = "TestTag"
    }

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q


//------------------------------------- Override Functions -----------------------------------------


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        // Set Click Listener for FAB.
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }

        //Comment this part to do view model test.
        _viewModel.authenticationState.observe(viewLifecycleOwner, Observer { updateUIAccordingToAuthenticationState(it) })

    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }


//------------------------------------- Navigation -------------------------------------------------


    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder())
        )
    }

//------------------------------------- Observer Functions -----------------------------------------


    private fun updateUIAccordingToAuthenticationState(state: AuthenticationState) {
        Log.i(AuthenticationActivity.LOG_TAG, "updateUIAccordingToAuthenticationState: run.")
        when (state) {
            AuthenticationState.AUTHENTICATED -> {
                // Jump to Main Page, if authenticated.
                Log.i(LOG_TAG_TEST, "The user has authenticated.")
            }
            else -> {
                // Change the UI to remind user to log in.
                Log.i(LOG_TAG_TEST, "There is no current user.")
                val intent = Intent(activity, AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }
    }


//------------------------------------- Recycler View ----------------------------------------------


    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

        // setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }



//------------------------------------- Menu Functions ---------------------------------------------


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                Log.i(LOG_TAG_TEST, "The logout button has been clicked.")
                AuthUI.getInstance().signOut(requireContext())
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }


}
