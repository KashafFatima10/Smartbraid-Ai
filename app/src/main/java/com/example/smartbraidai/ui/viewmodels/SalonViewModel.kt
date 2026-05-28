package com.example.smartbraidai.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbraidai.SuggestionEngine
import com.example.smartbraidai.UserSelection
import com.example.smartbraidai.data.models.*
import com.example.smartbraidai.data.repository.SalonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SalonViewModel(private val repository: SalonRepository = SalonRepository()) : ViewModel() {

    private val _artists = mutableStateOf<List<Artist>>(emptyList())
    val artists: State<List<Artist>> = _artists

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _pendingBooking = mutableStateOf<Booking?>(null)
    val pendingBooking: State<Booking?> = _pendingBooking

    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    private val _userPatterns = MutableStateFlow<List<Pattern>>(emptyList())
    val userPatterns: StateFlow<List<Pattern>> = _userPatterns

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings

    private val _clients = mutableStateOf<List<User>>(emptyList())
    val clients: State<List<User>> = _clients

    // --- AI CATEGORY & STATUS ---
    val aiCategory = mutableStateOf("Hair") // "Hair", "Makeup", or "Haircut"
    
    private val _aiStatusMessage = mutableStateOf("SmartBraid AI is ready")
    val aiStatusMessage: State<String> = _aiStatusMessage

    private val _generatedImageUrl = mutableStateOf<String?>(null)
    val generatedImageUrl: State<String?> = _generatedImageUrl

    // --- SELECTION STATES (AI SCREEN) ---
    val hairTexture = mutableStateOf("Straight")
    val hairColor = mutableStateOf("Black")
    val hairLength = mutableStateOf("Short")
    val hairVolume = mutableStateOf("Thin")

    val makeupType = mutableStateOf("Natural")
    val skinFinish = mutableStateOf("Matte")
    val eyeMakeup = mutableStateOf("Soft")
    val lipStyle = mutableStateOf("Glossy")

    val faceShape = mutableStateOf("Oval")
    val cutLength = mutableStateOf("Short")

    // --- SUGGESTION RESULT STATES ---
    private val _suggestionTitle = mutableStateOf("")
    val suggestionTitle: State<String> = _suggestionTitle

    private val _suggestionDesc = mutableStateOf("")
    val suggestionDesc: State<String> = _suggestionDesc

    private val _aiTips = mutableStateOf<List<String>>(emptyList())
    val aiTips: State<List<String>> = _aiTips

    private val _aiConfidence = mutableStateOf(0)
    val aiConfidence: State<Int> = _aiConfidence

    private val _aiMaintenance = mutableStateOf("Medium")
    val aiMaintenance: State<String> = _aiMaintenance

    private val _showSuggestionDialog = mutableStateOf(false)
    val showSuggestionDialog: State<Boolean> = _showSuggestionDialog

    init {
        fetchArtists()
    }

    fun fetchArtists() {
        viewModelScope.launch {
            _loading.value = true
            repository.getArtists().onSuccess { list ->
                _artists.value = list
            }
            _loading.value = false
        }
    }

    // --- RULE-BASED AI LOGIC ---

    fun onGenerateStyleClicked() {
        val selection = UserSelection(
            hairTexture = hairTexture.value,
            hairColor = hairColor.value,
            hairLength = hairLength.value,
            hairVolume = hairVolume.value,
            makeupType = makeupType.value,
            skinFinish = skinFinish.value,
            eyeMakeup = eyeMakeup.value,
            lipStyle = lipStyle.value,
            faceShape = faceShape.value,
            cutLength = cutLength.value
        )

        _loading.value = true
        _generatedImageUrl.value = null
        
        viewModelScope.launch {
            _aiStatusMessage.value = "SmartBraid AI is thinking..."
            delay(1000)
            
            _aiStatusMessage.value = "Analyzing your profile..."
            delay(1000)

            val suggestion = when (aiCategory.value) {
                "Hair" -> SuggestionEngine.generateHairSuggestion(selection)
                "Makeup" -> SuggestionEngine.generateMakeupSuggestion(selection)
                "Haircut" -> SuggestionEngine.generateHaircutSuggestion(selection)
                else -> null
            }

            if (suggestion != null) {
                _suggestionTitle.value = suggestion.title
                _suggestionDesc.value = suggestion.description
                _aiTips.value = suggestion.tips
                _aiConfidence.value = suggestion.confidence
                _aiMaintenance.value = suggestion.maintenanceLevel
                
                _showSuggestionDialog.value = true
            }
            
            _loading.value = false
            _aiStatusMessage.value = "Analysis Complete!"
        }
    }

    fun dismissSuggestionDialog() {
        _showSuggestionDialog.value = false
    }

    // --- STYLIST MANAGEMENT ---

    fun addStylist(context: Context, name: String, role: String, experience: String, description: String, services: String, imageUri: Uri?, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            var imageUrl = ""
            if (imageUri != null) {
                val uploadResult = repository.uploadImage(context, imageUri, "artists")
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull() ?: ""
            }
            val servicesList = services.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val artist = Artist(name = name, role = role, experience = experience, description = description, imageUrl = imageUrl, services = servicesList, rating = 5.0, status = "AVAILABLE TODAY")
            repository.addArtist(artist).onSuccess { fetchArtists(); onComplete(true, "Stylist added successfully!") }.onFailure { onComplete(false, "Failed to save stylist") }
            _loading.value = false
        }
    }

    fun updateStylist(context: Context, artistId: String, name: String, role: String, experience: String, description: String, services: String, imageUri: Uri?, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val existing = _artists.value.find { it.id == artistId } ?: return@launch
            var imageUrl = existing.imageUrl
            if (imageUri != null) {
                val uploadResult = repository.uploadImage(context, uri = imageUri, path = "artists")
                if (uploadResult.isSuccess) imageUrl = uploadResult.getOrNull() ?: imageUrl
            }
            val servicesList = services.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val updatedArtist = existing.copy(name = name, role = role, experience = experience, description = description, imageUrl = imageUrl, services = servicesList)
            repository.addArtist(updatedArtist).onSuccess { fetchArtists(); onComplete(true, "Stylist updated successfully!") }.onFailure { onComplete(false, "Failed to update stylist") }
            _loading.value = false
        }
    }

    fun updateArtistAvailability(artistId: String, availability: Map<String, Any>, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            repository.updateArtistAvailability(artistId, availability)
                .onSuccess {
                    fetchArtists()
                    onComplete(true, "Availability updated successfully!")
                }
                .onFailure {
                    onComplete(false, "Failed to update availability")
                }
            _loading.value = false
        }
    }

    // --- BOOKING FLOW ---

    fun prepareBooking(booking: Booking) { _pendingBooking.value = booking }
    fun confirmBooking(onSuccess: () -> Unit) {
        val booking = _pendingBooking.value?.copy(paymentStatus = "Paid", status = "Confirmed") ?: return
        viewModelScope.launch {
            _loading.value = true
            repository.createBooking(booking).onSuccess { onSuccess(); _pendingBooking.value = null }
            _loading.value = false
        }
    }

    fun observeUserBookings(userId: String) { viewModelScope.launch { repository.getUserBookings(userId).collect { list -> _userBookings.value = list.sortedByDescending { it.timestamp } } } }
    fun observeAllBookings() { viewModelScope.launch { repository.getAllBookings().collect { list -> _allBookings.value = list.sortedByDescending { it.timestamp } } } }
    fun updateStatus(bookingId: String, status: String) { viewModelScope.launch { repository.updateBookingStatus(bookingId, status) } }
    fun observeUserProfile(userId: String) { viewModelScope.launch { repository.getUserProfile(userId).collect { _userProfile.value = it } } }
    fun updateProfilePicture(context: Context, userId: String, uri: Uri) {
        viewModelScope.launch {
            _loading.value = true
            val uploadResult = repository.uploadImage(context, uri, "profiles")
            if (uploadResult.isSuccess) repository.updateUserProfile(userId, mapOf("profilePic" to (uploadResult.getOrNull() ?: "")))
            _loading.value = false
        }
    }
    fun fetchClients() { viewModelScope.launch { repository.getAllClients().onSuccess { _clients.value = it } } }
    fun updateUserPoints(userId: String, points: Int, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.updateUserRewards(userId, points)
            if (result.isSuccess) fetchClients()
            onComplete(result.isSuccess)
        }
    }
    fun observeNotifications(userId: String) { viewModelScope.launch { repository.getNotifications(userId).collect { _notifications.value = it } } }
    fun observeUserPatterns(userId: String) { viewModelScope.launch { repository.getUserPatterns(userId).collect { _userPatterns.value = it } } }
    fun deletePattern(patternId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            repository.deletePattern(patternId).onSuccess { onSuccess() }
            _loading.value = false
        }
    }

    fun generatePatternFromPrompt(userId: String, name: String, precision: Int, complexity: Int, symmetry: Boolean, sourcePrompt: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            val pattern = Pattern(userId = userId, name = name, precision = precision, complexity = complexity, symmetry = symmetry)
            repository.savePattern(pattern).onSuccess { onComplete(); observeUserPatterns(userId) }.onFailure { onError("Failed to save pattern") }
            _loading.value = false
        }
    }
}
