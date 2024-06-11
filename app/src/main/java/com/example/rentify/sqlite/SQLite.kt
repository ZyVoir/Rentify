package com.example.rentify.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Cart

class SQLite(var context: Context) : SQLiteOpenHelper(context, "rentify.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createCartQuery = """
            CREATE TABLE Cart (
                userID TEXT,
                itemID TEXT,
                duration INTEGER,
                isSelected INTEGER,
                PRIMARY KEY (userID, itemID)
            );
        """.trimIndent()

        db?.execSQL(createCartQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
        db?.execSQL("DROP TABLE IF EXISTS Cart")
        onCreate(db);
    }


    fun addToCart(cart: Cart) {
        var db = readableDatabase
        val query = """
            SELECT * FROM Cart WHERE ItemID = "${cart.itemID}" AND USERID = "${cart.userID}"
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        if (cursor.count > 0) {
            // user udh pernah nambah di cart sebelumnya add 1 aja duration nya
//            db = writableDatabase
//            val queryAdd = """
//        UPDATE Cart
//        SET duration = duration + 1
//        WHERE itemID = ? AND userID = ?;
//    """.trimIndent()
//
//            db.execSQL(queryAdd, arrayOf(cart.itemID, cart.userID))
        } else {
            db = writableDatabase
            val values = ContentValues().apply {
                put("userID", cart.userID)
                put("itemID", cart.itemID)
                put("duration", cart.duration)
                put("isSelected", if (cart.isSelected) 1 else 0)
            }

            db.insert("Cart", null, values)
        }

        cursor.close()
        db.close()
    }

    fun getAllCartByUserID(uid: String): ArrayList<Cart> {
        var cartList = ArrayList<Cart>()
        val db = readableDatabase

        val query = """
            SELECT * FROM Cart WHERE userID = "${uid}"
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        if (cursor.count > 0) {
            do {
                val userID = cursor.getString(cursor.getColumnIndexOrThrow("userID"))
                val itemID = cursor.getString(cursor.getColumnIndexOrThrow("itemID"))
                val duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"))
                val isSelected = cursor.getInt(cursor.getColumnIndexOrThrow("isSelected"))
                cartList.add(Cart(userID, itemID, duration, isSelected == 1))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return cartList
    }


    fun deleteItemCascadeCart(tid: String?) {
        if (tid != null) {
            var db = writableDatabase
            db.delete("Cart", "itemID = ?", arrayOf(tid))
            db.close()
        }
    }


    fun toggleItemSelection(cart: Cart, updatedBoolean: Boolean) {
        val updtBool = if (updatedBoolean) 1 else 0

        val db = writableDatabase
        val queryUpdate = """
        UPDATE Cart
        SET isSelected = $updtBool
        WHERE itemID = ? AND userID = ?;
           """.trimIndent()

        db.execSQL(queryUpdate, arrayOf(cart.itemID, cart.userID))
        db.close()
    }

    fun updateDuration(cart : Cart, newDuration: Int){
        val db = writableDatabase
        val queryUpdate = """
            UPDATE Cart
            SET duration = $newDuration
            WHERE itemID = ? AND userID = ?
        """.trimIndent()
        db.execSQL(queryUpdate, arrayOf(cart.itemID,cart.userID))
        db.close()
    }

    fun getTotalPrice(uid: String, callback: (Int) -> Unit) {
        var totalPrice = 0
        val db = readableDatabase
        val query = """SELECT * FROM Cart WHERE userID = ? AND isSelected = 1"""
        val cursor = db.rawQuery(query, arrayOf(uid))

        cursor.moveToFirst()
        if (cursor.count > 0) {
            var remainingItems = cursor.count

            do {
                val duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"))
                val itemID = cursor.getString(cursor.getColumnIndexOrThrow("itemID"))
                val fs = Firestore(context)
                fs.getItemFromDB(itemID).addOnSuccessListener { item ->
                    totalPrice += item.itemPrice * duration
                    remainingItems--

                    // When all items have been processed, call the callback
                    if (remainingItems == 0) {
                        callback(totalPrice)
                    }
                }.addOnFailureListener {
                    remainingItems--
                    if (remainingItems == 0) {
                        callback(totalPrice)
                    }
                }
            } while (cursor.moveToNext())
        } else {
            callback(totalPrice)
        }

        cursor.close()
        db.close()
    }


}