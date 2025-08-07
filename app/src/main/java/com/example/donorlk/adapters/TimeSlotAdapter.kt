import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.donorlk.R
import com.example.donorlk.models.TimeSlot
import com.google.android.material.card.MaterialCardView

class TimeSlotAdapter(
    private var timeSlots: MutableList<TimeSlot>,
    private val onSlotSelected: (TimeSlot, Int) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var selectedPosition = -1

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val slotTime: TextView = itemView.findViewById(R.id.slotTime)
        private val availableCount: TextView = itemView.findViewById(R.id.availableCount)
        private val cardView: MaterialCardView = itemView as MaterialCardView

        fun bind(timeSlot: TimeSlot, position: Int) {
            slotTime.text = timeSlot.time
            availableCount.text = "${timeSlot.availableCount} available"

            // Update card appearance based on availability and selection
            when {
                timeSlot.availableCount == 0 -> {
                    cardView.alpha = 0.5f
                    cardView.isClickable = false
                    cardView.strokeColor = itemView.context.getColor(R.color.gray)
                    availableCount.text = "Full"
                }
                position == selectedPosition -> {
                    cardView.alpha = 1.0f
                    cardView.strokeColor = itemView.context.getColor(R.color.app_red)
                    cardView.strokeWidth = 3
                    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.app_red_light))
                }
                else -> {
                    cardView.alpha = 1.0f
                    cardView.strokeColor = itemView.context.getColor(R.color.app_red)
                    cardView.strokeWidth = 1
                    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                }
            }

            cardView.setOnClickListener {
                if (timeSlot.availableCount > 0) {
                    val previousSelected = selectedPosition
                    selectedPosition = position

                    notifyItemChanged(previousSelected)
                    notifyItemChanged(selectedPosition)

                    onSlotSelected(timeSlot, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position], position)
    }

    override fun getItemCount() = timeSlots.size

    fun updateSlots(newSlots: List<TimeSlot>) {
        timeSlots.clear()
        timeSlots.addAll(newSlots)
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedSlot(): TimeSlot? {
        return if (selectedPosition >= 0) timeSlots[selectedPosition] else null
    }
}