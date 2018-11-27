package things.florian.com.piclockwidget

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.*

interface KeyedObject{
    var key: String
}

class FirebaseLiveData<T : KeyedObject> (q: Query?,
                                         val classToken: Class<T>): LiveData<List<T>>() {
    constructor(classToken: Class<T>) : this(null, classToken)
    val list = mutableListOf<T>()

    var query = q
    set(value) {
        field?.removeEventListener(queryListener)
        field = value
        field?.addChildEventListener(queryListener)
        list.clear()
        setValue(list)
    }

    //.orderByChild("height").startAt(3).addChildEventListener(
    private val queryListener = object : ChildEventListener{
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(FirebaseLiveData::class.simpleName, "loadPost:onCancelled", databaseError.toException())
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val indexPrevious = list.indexOfFirst{it.key == previousChildName}
            val index = list.indexOfFirst{it.key == dataSnapshot.key}

            if(index-1 != indexPrevious)
                addToPosition(previousChildName, list.removeAt(index))

            value = list
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val index = list.indexOfFirst{it.key == dataSnapshot.key}
            if(index >= 0 && index < list.size) {
                val data = dataSnapshot.getValue(classToken)

                if(data != null) {
                    data.key = dataSnapshot.key?:""
                    list[index] = data
                }
            }

            value = list
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val data = dataSnapshot.getValue(classToken)

            if(data != null) {
                data.key = dataSnapshot.key?:""
                addToPosition(previousChildName, data)
            }
            value = list
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val index = list.indexOfFirst{it.key == dataSnapshot.key}

            list.removeAt(index)
            value = list
        }


        fun addToPosition(previousChildName: String?, data: T){
            if (previousChildName == null) {
                if (list.isNotEmpty()) list.add(0, data)
                else list.add(data)
            } else {
                val index = list.indexOfFirst{it.key == previousChildName}
                //the index is the position of the previous node element
                if(index >= 0 && index < list.size) {
                    if(index == list.size -1)
                        list.add(data)
                    else
                        list.add(index +1, data)
                }
            }
        }

    }
    init {
        query?.addChildEventListener(queryListener)
    }

}