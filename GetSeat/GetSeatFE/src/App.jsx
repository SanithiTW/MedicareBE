import { BrowserRouter, Routes, Route } from "react-router-dom"
import LandingPage from "./pages/LandingPage"
import SearchBus from "./pages/SearchBus"
import SeatBooking from "./pages/SeatBooking"
import Checkout from "./pages/Checkout"
import Success from "./pages/Success"

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/search" element={<SearchBus />} />
        <Route path="/seat/:busId" element={<SeatBooking />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="/success" element={<Success />} />
      </Routes>
    </BrowserRouter>
  )
}