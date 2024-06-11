package com.example.rentify.firebase

import android.content.Context
import android.widget.Toast
import com.example.rentify.MainActivity
import com.example.rentify.model.Item
import com.example.rentify.model.Transaction
import com.example.rentify.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Firestore(context: Context) {
    val context = context
    val db = FirebaseFirestore.getInstance()

    fun initializeUserUponRegister(uid : String?,name : String, username : String, email : String,profilepic : String, isGoogleAuth : Boolean){
        if (uid != null) {
            var user = User(uid,email, name,username, profilepic,"","", "","","", 0,"",false, isGoogleAuth, 0.0, 0.0);
            MainActivity.currentUser = user;
            db.collection("users").document(uid).set(user)
        }
    }

    fun getUserFromDB(uid : String?) : Task<User> {
        var user = TaskCompletionSource<User>()
        if (uid != null) {
            val req = db.collection("users").document(uid)

            req.get().addOnCompleteListener { resp ->
                if (resp != null){
                    val data = resp.result
                    if (data != null && data["uid"] != null){
                        val uid = data["uid"] as String
                        val email = data["email"] as String
                        val name = data["name"] as String
                        val username = data["username"] as String
                        val profilePicLink = data["profilePicLink"] as String
                        val phoneNum = data["phoneNum"] as String
                        val street = data["addressStreet"] as String
                        val city = data["addressCity"] as String
                        val province = data["addressProvince"] as String
                        val country = data["addressCountry"] as String
                        val postCode = data["addressPostcode"] as Long
                        val paymentMethod = data["paymentMethod"] as String
                        val isCompletedProfile = data["completedProfile"] as Boolean
                        val isGoogleAuth = data["googleAuth"] as Boolean
                        val coordinateLat = data["coordinateLatitude"] as Double
                        val coordinateLng = data ["coordinateLongitude"] as Double
                        val instance = User(uid,email,name, username, profilePicLink,phoneNum, street, city,province,country,postCode.toInt(), paymentMethod,isCompletedProfile, isGoogleAuth, coordinateLat, coordinateLng)
                        user.setResult(instance)
                    }else {
                        user.setResult(null)
                    }
                }
            }.addOnFailureListener {
                println("failed :$it")
            }
        }
        return user.task
    }

    fun setCurrentUser(uid :String?) : Task<Boolean> {
        var isSuccess = TaskCompletionSource<Boolean>()
        getUserFromDB(uid).addOnCompleteListener { task ->
            if (task.isSuccessful){
                MainActivity.currentUser = task.result!!
                isSuccess.setResult(true)
            }
        }
        return isSuccess.task
    }

    fun updateProfile(user : User) : Task<Boolean>{
        var isSuccess = TaskCompletionSource<Boolean>()
        val ref = db.collection("users").document(user.uid)
        ref.set(user, SetOptions.merge()).addOnSuccessListener {
            isSuccess.setResult(true)
        }.addOnFailureListener{
            println(it.message.toString())
        }

        return isSuccess.task
    }

    fun insertItem(item : Item) : Task<Boolean>{
        var isSuccess = TaskCompletionSource<Boolean>()

        db.collection("items").document(item.itemID).set(item)
        isSuccess.setResult(true)
        return isSuccess.task
    }

    fun getItemFromDB(itemID : String?) : Task<Item> {
        var item = TaskCompletionSource<Item>()
        if (itemID != null){
            val req = db.collection("items").document(itemID)

            req.get().addOnCompleteListener { resp ->
                if (resp != null){
                    val data = resp.result
                    if (data != null && data["itemID"] != null){
                        val itemID = data["itemID"] as String
                        val tenantID = data["tenantID"] as String
                        val itemImg = data["itemImg"] as String
                        val itemName = data["itemName"] as String
                        val itemDesc = data["itemDesc"] as String
                        val itemCtg = data["itemCategory"] as String
                        val itemTime = data["itemTime"] as String
                        val itemPrice = data["itemPrice"] as Long
                        val isRented = data["rented"] as Boolean

                        val instance = Item(itemID,tenantID, itemImg, itemName, itemDesc,itemCtg,itemTime, itemPrice.toInt(), isRented)
                        item.setResult(instance)
                    }else {
                        item.setResult(null)
                    }
                }
            }.addOnFailureListener{
                println("failed :$it")
            }
        }
        return item.task
    }

    fun deleteItem(tid : String?) : Task<Boolean>{
        var isSuccess = TaskCompletionSource<Boolean>()
        if (tid != null){
            db.collection("items").document(tid).delete().addOnCompleteListener { dbtask->
                if (dbtask.isSuccessful){
                    val storage = FirebaseStorage.getInstance()
                    val imagePath = "/items/${tid}"
                    val imageRef: StorageReference = storage.reference.child(imagePath)
                    imageRef.delete()
                    isSuccess.setResult(true)
                }else {
                    isSuccess.setResult(false)
                }
            }
        }

        return isSuccess.task
    }

    fun insertTransaction(transaction : Transaction) : Task<Boolean>{
        var isSuccess = TaskCompletionSource<Boolean>()
        db.collection("transactions").document(transaction.transacionID).set(transaction)
        isSuccess.setResult(true)
        return isSuccess.task
    }

    fun updateItemRent(itemid : String ,isRent : Boolean) : Task<Boolean> {
        var isSuccess = TaskCompletionSource<Boolean>()
        val ref = db.collection("items").document(itemid)

        val update = hashMapOf<String,Any>(
            "rented" to isRent
        )

        ref.update(update).addOnCompleteListener {
            if (it.isSuccessful){
                isSuccess.setResult(true)
            }
            else {
                isSuccess.setResult(false)
            }
        }

        return isSuccess.task
    }

    fun updateTransactionStatus(transaction: Transaction) : Task<Boolean> {
        var isSuccess = TaskCompletionSource<Boolean>()
        val ref = db.collection("transactions").document(transaction.transacionID)

        val currStatusIndex = MainActivity.transactionStatus.indexOf(transaction.transactionStatus)
        if (currStatusIndex == MainActivity.transactionStatus.size-1){
            isSuccess.setResult(false)
            return isSuccess.task
        }
        var nextStatus : String = ""
        if (transaction.isExtend && transaction.transactionStatus == MainActivity.transactionStatus[0]){
            nextStatus = MainActivity.transactionStatus[2]
        }else {
            nextStatus = MainActivity.transactionStatus[currStatusIndex+1]
        }

        val update = hashMapOf<String,Any>(
            "transactionStatus" to nextStatus,
            "extend" to if (nextStatus == MainActivity.transactionStatus[MainActivity.transactionStatus.size-1]) false else transaction.isExtend
        )
        if (nextStatus == MainActivity.transactionStatus[MainActivity.transactionStatus.size-1]){
            updateItemRent(transaction.itemID, false).addOnSuccessListener {
                ref.update(update).addOnCompleteListener { updateTask->
                    if (updateTask.isSuccessful){
                        isSuccess.setResult(true)
                    }
                    else {
                        isSuccess.setResult(false)
                    }
                }
            }
        }else {
            ref.update(update).addOnCompleteListener {
                if (it.isSuccessful){
                    isSuccess.setResult(true)
                }
                else {
                    isSuccess.setResult(false)
                }
            }
        }


        return isSuccess.task
    }

    fun extendDuration(transaction: Transaction, extend : Int, timeFrame : String) : Task<Boolean>{
        var isSuccess = TaskCompletionSource<Boolean>()
        val newDueDate = MainActivity.getExtendedDueDate(transaction.transactionDueDate,extend,timeFrame)

        val ref = db.collection("transactions").document(transaction.transacionID)

        val update = hashMapOf<String,Any>(
            "transactionDueDate" to newDueDate,
            "transactionStatus" to MainActivity.transactionStatus[0],
            "extend" to true
        )

        ref.update(update).addOnCompleteListener {
            if (it.isSuccessful){
                isSuccess.setResult(true)
            }
            else {
                isSuccess.setResult(false)
            }
        }

        return isSuccess.task
    }

}