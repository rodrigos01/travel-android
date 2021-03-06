package com.combah.travel2.ui.trip.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.transition.AutoTransition
import com.combah.travel2.databinding.FragmentTransportationSetupBinding
import com.combah.travel2.ui.widget.DatePickerEditText
import com.combah.travel2.ui.widget.makeDatePickerDialog
import java.util.*


class TransportationSetupFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = AutoTransition()
        sharedElementReturnTransition = AutoTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val viewModel = ViewModelProvider(this)
            .get(TransportationSetupViewModel::class.java)

        val binding = FragmentTransportationSetupBinding.inflate(inflater, container, false)

        binding.pickerDepartureDate.setUpDate(viewModel.departureDate)
        binding.pickerReturnDate.setUpDate(
            viewModel.returnDate,
            minDateLiveData = viewModel.departureDate
        )

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    private fun DatePickerEditText.setUpDate(
        dateLiveData: MutableLiveData<Date>,
        minDateLiveData: LiveData<Date>? = null
    ) {
        dateLiveData.observe(viewLifecycleOwner) {
            date = it
        }
        minDateLiveData?.observe(viewLifecycleOwner) {
            minDate = it
        }
        setOnClickListener {
            makeDatePickerDialog(context, date, minDate = minDate) { newDate ->
                date = newDate
                dateLiveData.value = newDate
            }
        }
    }


}
