package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

/**
 * zoom to the user location after taking his permission
 * add style to the map
 * put a marker to location that the user selected
 * call this function after the user confirms on the selected location
 * */

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener {

    companion object {
        const val LOG_TAG: String = "SelectLocationFragment"
    }

    //Google Mapp
    private lateinit var mMap: GoogleMap
    private var selectedPoi: PointOfInterest? = null
    private lateinit var locationManager: LocationManager

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        setClickListeners()

        //add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    //------------------------------------- Click Functions --------------------------------------------


    private fun setClickListeners() {
        binding.savePoiBtn.setOnClickListener {
            selectedPoi?.let {
                _viewModel.showToast.postValue(getString(R.string.toast_info_save_success))
                onLocationSelected(it)
            }?:let{
                _viewModel.showToast.postValue(getString(R.string.toast_using_default_poi))
                onLocationSelected(getDefaultPOI())
            }
        }
    }

    private fun onLocationSelected(poi: PointOfInterest) {
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment(poi))
        )
    }


//------------------------------------- Google Map Functions ---------------------------------------


    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(LOG_TAG, "onMapReady: Run.")
        mMap = googleMap


        setMapLongClick(mMap)
        setMapPOIClick(mMap)
        setMapStyle(mMap)
        enableMyLocation()
        setMapZoomInMyLocation(mMap)
    }

    /** Set the initial location to Hong Kong. */
    private fun setMapZoomInMyLocation(map:GoogleMap) {
        try {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
        } catch(ex: SecurityException) {
            Log.d("myTag", "Security Exception, no location available")
        }
    }

    /**
     * Set Up the Add Marker (Long Click) Function for the Google Map.
     *
     * @param map
     * */
    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
            Locale.getDefault(),
            "Current Locate: (%1$.5f, %2$.5f)",
            latLng.latitude,
            latLng.longitude
        )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }
    }

    /**
     * Set up the Add Marker (Point Of Interest Click) Function for the Google Map. The previous POI
     * marker will be removed and add the new one.
     *
     * @param map
     * */
    private fun setMapPOIClick(map:GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            Log.d(LOG_TAG, "The name of poi is ${poi.name}, the latlng is ${poi.latLng}, the id is ${poi.placeId}")
            poiMarker.showInfoWindow()
            selectedPoi = poi
            Toast.makeText(requireContext(), "The location ${poi.name} has been chosen", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Set up the style of the Google Map.
     *
     * @param map
     * */
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))

            if (!success) {
                Log.e(LOG_TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(LOG_TAG, "Can't find style. Error: ", e)
        }
    }


//------------------------------------- Menu Functions ---------------------------------------------


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    /**
     * The method selects the menu item and change the map type.
     *
     * @param item
     * @return Boolean Value
     * */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            Log.i(LOG_TAG, "The normal map has been chosen.")
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            Log.i(LOG_TAG, "The hybrid map has been chosen.")
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            Log.i(LOG_TAG, "The satellite map has been chosen.")
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            Log.i(LOG_TAG, "The terrain map has been chosen.")
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


//------------------------------------- Permission Functions ---------------------------------------


    /**
     * Enable the current Location. If the permission deny, require for location permission.
     * */
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {
            _viewModel.showToast.postValue(getString(R.string.location_jump_to_current))
            mMap.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.REQUEST_LOCATION_PERMISSION
            )
        }
    }

    /**
     * Receive the decision of User and make further action.
     * */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == Constants.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), getString(R.string.location_permission_no_granted), Toast.LENGTH_SHORT).show()
            }
        }
    }


//------------------------------------- Default POI ------------------------------------------------


    private fun getDefaultPOI() =
        PointOfInterest(
            Constants.DEFAULT_POI_LATLNG,
            Constants.DEFAULT_POI_ID,
            Constants.DEFAULT_POI_NAME
        )


//------------------------------------- Location Listener Override Functions -----------------------


    override fun onLocationChanged(location: Location) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                Constants.ZOOM_LEVEL
            )
        )
    }


}
