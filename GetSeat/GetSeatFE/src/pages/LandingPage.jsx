import Navbar from "../components/Navbar"
import { useNavigate } from "react-router-dom"

export default function LandingPage() {
  const navigate = useNavigate()

  return (
    <div>
      <Navbar/>
      <section className="h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-5xl font-bold mb-6">Book Bus Tickets Easily</h1>
          <p className="text-dark-300 mb-10">
            Reserve your seats in seconds
          </p>
          <button
            onClick={() => navigate("/search")}
            className="bg-blue-600 px-8 py-3 rounded-lg hover:bg-blue-700"
          >
            Search Buses
          </button>
        </div>
      </section>
    </div>
  )
}