package pl.org.seva.compass.main.extension

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

inline fun <reified R : ViewModel> FragmentActivity.viewModel() = lazy { getViewModel<R>() }

inline fun <reified R : ViewModel> FragmentActivity.getViewModel() =
        ViewModelProviders.of(this).get(R::class.java)
