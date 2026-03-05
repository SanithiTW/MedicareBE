import Navbar from "../components/Navbar"

export default function Success() {
  return (
    <div>
      <Navbar/>
      <div className="h-screen flex flex-col items-center justify-center">
        <h1 className="text-4xl font-bold text-green-400 mb-4">
          Payment Successful!
        </h1>
        <p className="text-dark-300">
          Your bus seat booking has been confirmed.
        </p>
      </div>
    </div>
  )
}