import Logo from "../assets/GetSeat_logo.jpg"
import { Link } from "react-router-dom"

export default function Navbar() {
  return (
    <nav className="bg-dark-900 border-b border-dark-700">
      <div className="max-w-7xl mx-auto flex justify-between items-center p-4">
        <div className="flex items-center gap-3">
          <img src={Logo} className="w-10"/>
          <h1 className="text-xl font-bold">GetSeat</h1>
        </div>
        <div className="flex gap-6 text-sm">
          <Link to="/">Home</Link>
          <Link to="/search">Book Seats</Link>
        </div>
      </div>
    </nav>
  )
}