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

class EditOperatorController : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var backButton: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val operators = mutableListOf<Operator>()
    private lateinit var adapter: OperatorAdapter

    data class Operator(val name: String, val email: String, val docId: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_operator)

        listView = findViewById(R.id.subAdminListView)
        backButton = findViewById(R.id.backButton)

        adapter = OperatorAdapter()
        listView.adapter = adapter

        fetchOperators()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchOperators() {
        db.collection("users")
            .whereEqualTo("role", "Operator")
            .get()
            .addOnSuccessListener { result ->
                operators.clear()
                for (doc in result) {
                    val name = doc.getString("name") ?: continue
                    val email = doc.getString("email") ?: continue
                    operators.add(Operator(name, email, doc.id))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load operators", Toast.LENGTH_SHORT).show()
            }
    }

    inner class OperatorAdapter : BaseAdapter() {
        override fun getCount(): Int = operators.size
        override fun getItem(position: Int): Any = operators[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@EditOperatorController)
                .inflate(R.layout.list_item_sub_admin, parent, false)

            val operator = operators[position]
            val nameText = view.findViewById<TextView>(R.id.subAdminName)
            val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)

            nameText.text = operator.name


            deleteIcon.setOnClickListener {
                AlertDialog.Builder(this@EditOperatorController)
                    .setTitle("Delete Operator")
                    .setMessage("Are you sure you want to delete ${operator.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.collection("users").document(operator.docId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@EditOperatorController,
                                    "${operator.name} deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                                fetchOperators()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@EditOperatorController,
                                    "Failed to delete ${operator.name}",
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
