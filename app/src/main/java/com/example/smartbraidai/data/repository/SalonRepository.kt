package com.example.smartbraidai.data.repository

import android.content.Context
import android.net.Uri
import com.example.smartbraidai.BuildConfig
import com.example.smartbraidai.data.models.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class SalonRepository {
    private val db = FirebaseFirestore.getInstance()

    // --- ARTISTS ---
    suspend fun getArtists(): Result<List<Artist>> = try {
        val snapshot = db.collection("artists").get().await()
        Result.success(snapshot.toObjects(Artist::class.java))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addArtist(artist: Artist): Result<Unit> = try {
        val docId = artist.id.ifEmpty { db.collection("artists").document().id }
        db.collection("artists").document(docId).set(artist.copy(id = docId)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // --- IMAGE UPLOAD ---
    suspend fun uploadImage(context: Context, uri: Uri, path: String): Result<String> = try {
        withContext(Dispatchers.IO) {
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME.trim()
            val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET.trim()
            if (cloudName.isBlank() || uploadPreset.isBlank()) throw IllegalStateException("Cloudinary config missing.")

            val connection = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload").openConnection() as HttpURLConnection
            val boundary = "----SmartBraidAi${UUID.randomUUID().toString().replace("-", "")}"
            connection.apply {
                doInput = true
                doOutput = true
                useCaches = false
                requestMethod = "POST"
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }
            DataOutputStream(connection.outputStream).use { output ->
                fun writeText(text: String) { output.write(text.toByteArray(Charsets.UTF_8)) }
                writeText("--$boundary\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\n$uploadPreset\r\n")
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                writeText("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"upload.jpg\"\r\nContent-Type: $mimeType\r\n\r\n")
                context.contentResolver.openInputStream(uri)?.use { it.copyTo(output) }
                writeText("\r\n--$boundary--\r\n")
                output.flush()
            }
            val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().readText()
            if (connection.responseCode !in 200..299) throw Exception("Upload failed: $body")
            Result.success(JSONObject(body).getString("secure_url"))
        }
    } catch (e: Exception) { Result.failure(e) }

    // --- USER PROFILE ---
    fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ -> trySend(snapshot?.toObject(User::class.java)) }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> = try {
        db.collection("users").document(userId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // --- BOOKINGS ---
    suspend fun createBooking(booking: Booking): Result<String> = try {
        val docRef = db.collection("bookings").document()
        docRef.set(booking.copy(id = docRef.id)).await()
        Result.success(docRef.id)
    } catch (e: Exception) { Result.failure(e) }

    fun getUserBookings(userId: String): Flow<List<Booking>> = callbackFlow {
        val listener = db.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ -> snapshot?.let { trySend(it.toObjects(Booking::class.java)) } }
        awaitClose { listener.remove() }
    }

    fun getAllBookings(): Flow<List<Booking>> = callbackFlow {
        val listener = db.collection("bookings")
            .addSnapshotListener { snapshot, _ -> snapshot?.let { trySend(it.toObjects(Booking::class.java)) } }
        awaitClose { listener.remove() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = try {
        val bookingSnapshot = db.collection("bookings").document(bookingId).get().await()
        val userId = bookingSnapshot.getString("userId") ?: ""
        db.collection("bookings").document(bookingId).update("status", status).await()
        if (status.equals("Completed", ignoreCase = true)) {
            updateUserRewards(userId, 150)
        }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // --- LOYALTY ---
    suspend fun updateUserRewards(userId: String, points: Int): Result<Unit> = try {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { trans ->
            val snapshot = trans.get(userRef)
            val current = snapshot.getLong("rewardPoints") ?: 0
            trans.update(userRef, "rewardPoints", (current + points).coerceAtLeast(0))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getAllClients(): Result<List<User>> = try {
        val snapshot = db.collection("users").whereEqualTo("role", "customer").get().await()
        Result.success(snapshot.toObjects(User::class.java))
    } catch (e: Exception) { Result.failure(e) }

    // --- NOTIFICATIONS ---
    fun getNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ -> snapshot?.let { trySend(it.toObjects(AppNotification::class.java)) } }
        awaitClose { listener.remove() }
    }

    // --- PATTERNS ---
    suspend fun savePattern(pattern: Pattern): Result<Unit> = try {
        val docId = pattern.id.ifEmpty { db.collection("patterns").document().id }
        db.collection("patterns").document(docId).set(pattern.copy(id = docId)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deletePattern(patternId: String): Result<Unit> = try {
        db.collection("patterns").document(patternId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    fun getUserPatterns(userId: String): Flow<List<Pattern>> = callbackFlow {
        val listener = db.collection("patterns")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ -> snapshot?.let { trySend(it.toObjects(Pattern::class.java)) } }
        awaitClose { listener.remove() }
    }

    // --- AVAILABILITY ---
    suspend fun updateArtistAvailability(artistId: String, availability: Map<String, Any>): Result<Unit> = try {
        db.collection("artists").document(artistId).update("availability", availability).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // --- SEEDING ---
    suspend fun seedDatabase(): Result<Unit> = try {
        val artists = listOf(
            Artist("1", "Sarah Jenkins", "Lead Stylist", 4.9, "https://images.unsplash.com/photo-1595959183082-a8a64e4e27f9", listOf("Knotless Braids"), "8+ Years", "Expert.", "AVAILABLE TODAY"),
            Artist("2", "Marcus Thorne", "Master Barber", 4.8, "https://images.unsplash.com/photo-1567894340315-735d7c361db0", listOf("Haircut"), "12+ Years", "Pro.", "TOP RATED")
        )
        for (a in artists) db.collection("artists").document(a.id).set(a).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
