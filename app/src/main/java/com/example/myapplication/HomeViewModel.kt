import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class HomeViewModel(user: String) : ViewModel() {
    private val sessionReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("users").child(user).child("session")

    private val _isUserInSession = MutableLiveData<Boolean>()
    val isUserInSession: LiveData<Boolean> get() = _isUserInSession

    init {
        sessionReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isUserInSession.value = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {
                _isUserInSession.value = false
            }
        })
    }
}