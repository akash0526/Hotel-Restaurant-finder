package com.placefinder.directory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.placefinder.directory.data.models.Place
import com.placefinder.directory.data.models.PlaceCategory
import com.placefinder.directory.data.models.PlaceStatus
import com.placefinder.directory.data.repository.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Backs the "Add / edit a place" editor form. The form is a [Place] snapshot that each
 * `setXxx` method writes through; [save] persists it (name/address are required).
 */
class PlaceEditorViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _form = MutableStateFlow(
        Place(
            name = "",
            address = "",
            city = "",
            category = PlaceCategory.HOTEL,
            isOwnedByUser = true,
        )
    )
    val form: StateFlow<Place> = _form

    fun loadPlace(id: String) {
        viewModelScope.launch {
            val place = repository.getPlaceById(id) ?: return@launch
            _form.value = place.copy(isOwnedByUser = true)
        }
    }

    fun setName(name: String) { _form.value = _form.value.copy(name = name) }
    fun setAddress(address: String) { _form.value = _form.value.copy(address = address) }
    fun setCity(city: String) { _form.value = _form.value.copy(city = city) }
    fun setRegion(region: String) { _form.value = _form.value.copy(region = region.ifBlank { null }) }
    fun setCategory(category: PlaceCategory) { _form.value = _form.value.copy(category = category) }
    fun setPhone(phone: String) { _form.value = _form.value.copy(phone = phone.ifBlank { null }) }
    fun setEmail(email: String) { _form.value = _form.value.copy(email = email.ifBlank { null }) }
    fun setWebsite(website: String) { _form.value = _form.value.copy(website = website.ifBlank { null }) }
    fun setDescription(description: String) { _form.value = _form.value.copy(description = description) }
    fun setStatus(status: PlaceStatus) { _form.value = _form.value.copy(status = status) }
    fun setRoomsFree(roomsFree: Int?) { _form.value = _form.value.copy(roomsFree = roomsFree) }
    fun setAvailabilityNote(note: String?) { _form.value = _form.value.copy(availabilityNote = note) }

    /** Persist the form; called when the form is valid. */
    fun save() {
        viewModelScope.launch {
            val place = _form.value.copy(
                isOwnedByUser = true,
                updatedAt = System.currentTimeMillis(),
            )
            repository.savePlace(place)
        }
    }

    val isNameValid: Boolean get() = _form.value.name.isNotBlank()
    val isAddressValid: Boolean get() = _form.value.address.isNotBlank()
    val isValid: Boolean get() = isNameValid && isAddressValid
}
