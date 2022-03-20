package com.udacity.project4.locationreminders.reminderslist

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.BuildConfig
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationState
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.jar.Manifest

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
            Log.i(LOG_TAG_TEST, "FAB has been clicked.")
            navigateToAddReminder()
        }

        //Comment this part to do view model test.
        _viewModel.authenticationState.observe(viewLifecycleOwner, Observer { updateUIAccordingToAuthenticationState(it) })

        checkPermissions()
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


//--------------------------- GeoFencing Permission Request Functions ------------------------------


    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkPermissions() {
        Log.d(SaveReminderFragment.LOG_TAG, "checkPermissionsAndStartGeoFencing: run")
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            Log.d(SaveReminderFragment.LOG_TAG,"Permission Granted.")
            Toast.makeText(requireContext(), "Permission Granted.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(SaveReminderFragment.LOG_TAG,"Permission Deny.")
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    /**
     * Check whether the foreground and background permissions approved.
     *
     * @return If both permissions have approved, True; else, False.
     * */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {

        // foreground permission.
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION))
        // background permission according to the Android Version.
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireContext(),
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }

        return foregroundLocationApproved && backgroundPermissionApproved

    }

    /**
     *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
     */
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        Log.d(SaveReminderFragment.LOG_TAG, "requestForegroundAndBackgroundLocationPermissions: run.")

        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    /**
     * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
     * the background permission as well.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(SaveReminderFragment.LOG_TAG, "onRequestPermissionResult: run.")

        if (
            grantResults.isEmpty() ||
            grantResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE && grantResults[Constants.BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED))
        {
            // Permission denied.
            Snackbar.make(binding.listContainer, R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    // The BuildConfig.APPLICATION_ID has been replaced by BuildConfig.LIBRARY_PACKAGE_NAME
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.LIBRARY_PACKAGE_NAME, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            // Permission granted
            Toast.makeText(requireContext(), "Permission Granted.", Toast.LENGTH_SHORT).show()
        }
    }
}
