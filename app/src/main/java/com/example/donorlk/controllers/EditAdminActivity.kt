package com.example.donorlk.controllers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.donorlk.R
import com.google.firebase.firestore.FirebaseFirestore

class EditAdminActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var backButton: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val subAdmins = mutableListOf<SubAdmin>()
    private lateinit var adapter: SubAdminAdapter

    data class SubAdmin(val name: String, val email: String, val docId: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_admin)

        listView = findViewById(R.id.subAdminListView)
        backButton = findViewById(R.id.backButton)

        adapter = SubAdminAdapter()
        listView.adapter = adapter

        fetchSubAdmins()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchSubAdmins() {
        db.collection("users")
            .whereEqualTo("role", "Sub Admin")
            .get()
            .addOnSuccessListener { result ->
                subAdmins.clear()
                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val email = doc.getString("email") ?: continue
                    subAdmins.add(SubAdmin(name, email, doc.id))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load sub-admins", Toast.LENGTH_SHORT).show()
            }
    }

    inner class SubAdminAdapter : BaseAdapter() {
        override fun getCount(): Int = subAdmins.size
        override fun getItem(position: Int): Any = subAdmins[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@EditAdminActivity)
                .inflate(R.layout.list_item_sub_admin, parent, false)

            val subAdmin = subAdmins[position]
            val nameText = view.findViewById<TextView>(R.id.subAdminName)
            val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)

            nameText.text = subAdmin.name

            deleteIcon.setOnClickListener {
                AlertDialog.Builder(this@EditAdminActivity)
                    .setTitle("Delete Admin")
                    .setMessage("Are you sure you want to delete ${subAdmin.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.collection("users").document(subAdmin.docId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@EditAdminActivity,
                                    "${subAdmin.name} deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                                fetchSubAdmins()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@EditAdminActivity,
                                    "Failed to delete ${subAdmin.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            return view
        }
    }
}
