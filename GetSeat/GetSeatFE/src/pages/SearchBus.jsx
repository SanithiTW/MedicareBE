import Navbar from "../components/Navbar"
import { useNavigate } from "react-router-dom"
import { useState } from "react"

export default function SearchBus() {
  const navigate = useNavigate()
  const [from, setFrom] = useState("")
  const [to, setTo] = useState("")
  const [date, setDate] = useState("")

  return (
    <div>
      <Navbar/>
      <div className="max-w-5xl mx-auto mt-10">
        <h2 className="text-2xl font-bold mb-6">Search Bus</h2>
        <div className="bg-dark-800 p-6 rounded-lg grid grid-cols-1 md:grid-cols-3 gap-4">
          <input placeholder="From" value={from} onChange={e=>setFrom(e.target.value)} className="p-2"/>
          <input placeholder="To" value={to} onChange={e=>setTo(e.target.value)} className="p-2"/>
          <input type="date" value={date} onChange={e=>setDate(e.target.value)} className="p-2"/>
          <button
            onClick={() => navigate("/seat/1")}
            className="bg-blue-600 py-3 rounded col-span-full mt-4"
          >
            Search
          </button>
        </div>
      </div>
    </div>
  )
}