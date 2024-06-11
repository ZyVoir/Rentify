package com.example.rentify.navigation_bar.transaction_folder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.adapter.transaction_adapter
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Transaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.material.internal.ViewUtils
import com.google.firebase.firestore.ListenerRegistration

class transaction_page : Fragment(), transaction_adapter.OnTransactionClickListener {

    private lateinit var Ongoing_recyclerView: RecyclerView
    private lateinit var History_recyclerView: RecyclerView
    private lateinit var Ongoing_adapter: transaction_adapter
    private lateinit var history_adapter: transaction_adapter
    private var transactionList = mutableListOf<Transaction>()
    private var historyList = mutableListOf<Transaction>()
    private var tenantCoordinate: ArrayList<Double> = arrayListOf(0.0 , 0.0)
    private var renterCoordinate: ArrayList<Double> = arrayListOf(0.0 , 0.0)
    private lateinit var dialogView: View
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var listenerRegistration: ListenerRegistration? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_page, container, false)
        dialogView = layoutInflater.inflate(R.layout.custom_on_the_way_map_dialog, null)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        alertDialog = dialogBuilder.create()

//        ongoing recycler view
        Ongoing_recyclerView=view.findViewById(R.id.on_going_recycler_view)
        Ongoing_adapter = transaction_adapter(transactionList)
        Ongoing_recyclerView.adapter= Ongoing_adapter
        Ongoing_recyclerView.layoutManager= LinearLayoutManager(requireContext())
        Ongoing_adapter.setOnTransactionClickListener(this)



//        history recycler view
        History_recyclerView=view.findViewById(R.id.history_recycler_view)
        history_adapter = transaction_adapter(historyList)
        History_recyclerView.adapter= history_adapter
        History_recyclerView.layoutManager= LinearLayoutManager(requireContext())

        val fs = Firestore(requireContext())
        listenerRegistration = fs.db.collection("transactions").addSnapshotListener{ snapshots, e ->
            if (e != null){
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                transactionList.clear()
                historyList.clear()
                for (doc in snapshots.documents) {
                    val transaction = doc.toObject(Transaction::class.java)
                    if (transaction != null && (transaction.tenantID==MainActivity.currentUser.uid || transaction.renterID==MainActivity.currentUser.uid) ) {
                        if (!transaction.transactionStatus.equals(MainActivity.transactionStatus[MainActivity.transactionStatus.size-1])){
                            transactionList.add(transaction)
                        }
                        else  {
                            historyList.add(transaction)
                        }

                    }

                }
                Ongoing_adapter.notifyDataSetChanged()
                history_adapter.notifyDataSetChanged()
            }
        }



            return  view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }

    override fun onTransactionClick(transaction: Transaction) {

        if(transaction.transactionStatus == MainActivity.transactionStatus[0] ){
            if(transaction.renterID == MainActivity.currentUser.uid){
                alert_Status_Waiting(requireContext(), transaction)
            }else{
               Toast.makeText(requireContext(), "Waiting renter's payment", Toast.LENGTH_SHORT).show()
            }
        }else if(transaction.transactionStatus == MainActivity.transactionStatus[1] || transaction.transactionStatus == MainActivity.transactionStatus[3]){
            setLocation(transaction).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    alert_Status_Delivery(requireContext(),transaction)
                }
            }
        }else if(transaction.transactionStatus == MainActivity.transactionStatus[2]){
            if(transaction.renterID == MainActivity.currentUser.uid){
                alert_Status_OnRent(requireContext(), transaction)
            }else{
                Toast.makeText(requireContext(), "Renter still on rent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun alert_Status_Waiting(context: Context, transaction: Transaction){
        val dialogView = layoutInflater.inflate(R.layout.custom_waiting_payment_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context).setView(dialogView)
        val alertDialog = dialogBuilder.create()


        val noButton = dialogView.findViewById<Button>(R.id.checkoutBtn_NO)
        val yesButton = dialogView.findViewById<Button>(R.id.checkoutBtn_YES)
        val itemName =  dialogView.findViewById<TextView>(R.id.item_name_textView)
        val paymentMethod =  dialogView.findViewById<TextView>(R.id.payment_method_textview)
        val countdown =  dialogView.findViewById<TextView>(R.id.waiting_payment_countdown)

        itemName.text = transaction.itemName
        paymentMethod.text = MainActivity.currentUser.paymentMethod

        countdown.text = "Complete the payment before\n${transaction.transactionDate} 23:59"


        noButton.setOnClickListener {
            alertDialog.dismiss()
        }

        yesButton.setOnClickListener {
            val db=Firestore(context)
            db.updateTransactionStatus(transaction).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(requireContext(),"verify successfull", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            }
        }

        alertDialog.show()

        val width = (360 * resources.displayMetrics.density).toInt()
        val height = (260 * resources.displayMetrics.density).toInt()
        alertDialog.window?.setLayout(width, height)
    }

    @SuppressLint("RestrictedApi")
    fun alert_Status_OnRent(context: Context, transaction: Transaction){
        val dialogView = layoutInflater.inflate(R.layout.custom_extend_rent_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context).setView(dialogView)
        val alertDialog = dialogBuilder.create()


        val returnButton = dialogView.findViewById<Button>(R.id.return_item_button)
        val extendButton = dialogView.findViewById<Button>(R.id.extend_duration)
        val itemName =  dialogView.findViewById<TextView>(R.id.item_name_textView)
        val countDuration = dialogView.findViewById<TextView>(R.id.countdown_number)
        val timeStampDuration =  dialogView.findViewById<TextView>(R.id.countdown_timestamp)
        val duration_extend = dialogView.findViewById<EditText>(R.id.rent_duration_editText)
        val timeStamp_extend = dialogView.findViewById<TextView>(R.id.time_stamp_textView)
        val totalPrice = dialogView.findViewById<TextView>(R.id.extendrent_tv_totalprice)

        val totalDay = MainActivity.getTimeDifference(transaction.transactionDate, transaction.transactionDueDate)
        var remaining = ""
        itemName.text = transaction.itemName

        val years: Int = totalDay / 365
        val months: Int = (totalDay % 365)/30
        val days: Int = totalDay % 30

        if(years>0){
            remaining += "${years} Years"
        }
        if(months>0){
            remaining += " ${months} Months"
        }
        if(days>0){
            remaining += " ${days} Days"
        }

        countDuration.text= remaining
        timeStampDuration.text=""

        duration_extend.setText("1")
        totalPrice.text = MainActivity.formatRupiah(duration_extend.text.toString().toInt() * transaction.itemPrice)
        timeStamp_extend.text = transaction.itemTime

        duration_extend.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                if (duration_extend.text.isEmpty()){
                    Toast.makeText(requireContext(), "Cannot be empty", Toast.LENGTH_SHORT).show()
                }
                else {
                    totalPrice.text = MainActivity.formatRupiah(duration_extend.text.toString().toInt() * transaction.itemPrice)
                    ViewUtils.hideKeyboard(v)
                }
                true
            }
            else {
                false
            }
        }

        returnButton.setOnClickListener {
            val db=Firestore(context)
            db.updateTransactionStatus(transaction).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(requireContext(),"Proceed to return item", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            }
            alertDialog.dismiss()
        }

        extendButton.setOnClickListener {
            if (duration_extend.text.toString().isEmpty()){
                Toast.makeText(context, "Duration must not be empty", Toast.LENGTH_SHORT).show()
            }
            else {
                val duration = duration_extend.text.toString().toInt()
                if (transaction.itemTime == "Day" && duration > 30){
                    Toast.makeText(context,"Max 30 Days", Toast.LENGTH_SHORT).show()
                }else if (transaction.itemTime == "Month" && duration > 12){
                    Toast.makeText(context,"Max 12 Months", Toast.LENGTH_SHORT).show()
                }else if (transaction.itemTime == "Year" && duration > 10){
                    Toast.makeText(context,"Max 10 Years", Toast.LENGTH_SHORT).show()
                }
                else {
                    val fs = Firestore(context)
                    fs.extendDuration(transaction,duration, transaction.itemTime).addOnCompleteListener {task->
                        if (task.isSuccessful){
                            Toast.makeText(context, "Extend request has been sent", Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                        }

                    }
                }
            }
        }

        alertDialog.show()

        val width = (390 * resources.displayMetrics.density).toInt()
        val height = (310 * resources.displayMetrics.density).toInt()
        alertDialog.window?.setLayout(width, height)
    }

    fun alert_Status_Delivery(context: Context, transaction: Transaction){



        val completeButton = dialogView.findViewById<Button>(R.id.complete_delivery_button)
        val itemName = dialogView.findViewById<TextView>(R.id.item_name_textView)
        val status = dialogView.findViewById<TextView>(R.id.status_textview)

        if ((transaction.transactionStatus == MainActivity.transactionStatus[1] && transaction.tenantID == MainActivity.currentUser.uid)
            || (transaction.transactionStatus == MainActivity.transactionStatus[3] && transaction.renterID == MainActivity.currentUser.uid)){
            completeButton.visibility = View.INVISIBLE
        }else {
            completeButton.visibility = View.VISIBLE
        }

        itemName.text= transaction.itemName
        status.text= transaction.transactionStatus

//        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(context)
//        var mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)


        mapFragment.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(googleMap: GoogleMap) {
                googleMap.clear()
                val renter = LatLng(renterCoordinate[0], renterCoordinate[1])
                val tenant = LatLng(tenantCoordinate[0], tenantCoordinate[1])

                googleMap.addMarker(MarkerOptions().position(renter).title("Renter"))
                googleMap.addMarker(MarkerOptions().position(tenant).title("Tenant"))

                val builder = LatLngBounds.Builder()
                builder.include(renter)
                builder.include(tenant)
                val bounds = builder.build()

                val padding = 100
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                googleMap.moveCamera(cameraUpdate)
            }
        })



        completeButton.setOnClickListener {
            val fs= Firestore(context)
            fs.updateTransactionStatus(transaction).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    Toast.makeText(context,"${transaction.transactionStatus} successfull", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            }

        }


        alertDialog.show()

    }


//    override fun onMapReady(googleMap: GoogleMap) {
//        map = googleMap
//        val renter = LatLng(renterCoordinate[0], renterCoordinate[1])
//        val tenant = LatLng(tenantCoordinate[0], tenantCoordinate[1])
//
//        map.addMarker(MarkerOptions().position(renter).title("Renter"))
//        map.addMarker(MarkerOptions().position(tenant).title("Tenant"))
//
//        // Create a LatLngBounds to include both markers
//        val builder = LatLngBounds.Builder()
//        builder.include(renter)
//        builder.include(tenant)
//        val bounds = builder.build()
//
//        // Move the camera to show both markers
//        val padding = 100 // offset from edges of the map in pixels
//        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
//        map.moveCamera(cameraUpdate)
//    }


    fun setLocation(transaction : Transaction) : Task<Boolean> {
        var isSucess = TaskCompletionSource<Boolean>()

        renterCoordinate[0]= transaction.renterLat
        renterCoordinate[1]= transaction.renterLng
        tenantCoordinate[0]= transaction.tenantLat
        tenantCoordinate[1]= transaction.tenantLng

        isSucess.setResult(true)
        return isSucess.task
    }


}