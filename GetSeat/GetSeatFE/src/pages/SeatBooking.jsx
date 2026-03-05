import Navbar from "../components/Navbar"
import { useState } from "react"
import { useNavigate } from "react-router-dom"

export default function SeatBooking() {
  const [selected, setSelected] = useState([])
  const navigate = useNavigate()
  const seats = Array.from({ length: 40 }, (_, i) => i + 1)

  const toggleSeat = (seat) => {
    if (selected.includes(seat)) {
      setSelected(selected.filter(s => s !== seat))
    } else {
      setSelected([...selected, seat])
    }
  }

  return (
    <div>
      <Navbar/>
      <div className="max-w-5xl mx-auto p-10">
        <h2 className="text-2xl mb-6">Select Seats</h2>
        <div className="grid grid-cols-4 gap-4 mb-6">
          {seats.map(seat => (
            <button
              key={seat}
              onClick={() => toggleSeat(seat)}
              className={`p-4 rounded ${
                selected.includes(seat) ? "bg-green-500" : "bg-dark-700"
              }`}
            >
              {seat}
            </button>
          ))}
        </div>
        <button
          onClick={() => navigate("/checkout")}
          className="bg-blue-600 px-6 py-3 rounded"
        >
          Proceed to Checkout ({selected.length} Seats)
        </button>
      </div>
    </div>
  )
}