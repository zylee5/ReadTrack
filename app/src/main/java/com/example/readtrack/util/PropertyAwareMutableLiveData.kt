package com.example.readtrack.util

import androidx.databinding.BaseObservable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData

/***
 * A MutableLiveData that gets notified when a property of an object wrapped by it is updated
 */
class PropertyAwareMutableLiveData<T : BaseObservable> : MutableLiveData<T>() {
    private val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            value = value
        }
    }

    override fun setValue(value: T?) {
        super.setValue(value)
        value?.addOnPropertyChangedCallback(callback)
    }
}