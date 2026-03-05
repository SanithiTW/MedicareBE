import Navbar from "../components/Navbar"
import axios from "axios"

export default function Checkout() {

  async function pay() {
    try {
      const res = await axios.get(
        "http://localhost:8080/api/payment/hash?orderId=BUS123&amount=2000"
      )
      const hash = res.data.hash

      const payment = {
        sandbox: true,
        merchant_id: "121XXXX",
        return_url: "http://localhost:5173/success",
        cancel_url: "http://localhost:5173",
        notify_url: "https://yourdomain.com/api/payhere/notify",
        order_id: "BUS123",
        items: "Bus Ticket",
        currency: "LKR",
        amount: "2000",
        hash: hash,
        first_name: "Customer",
        last_name: "User",
        email: "test@gmail.com",
        phone: "0771234567",
        address: "Colombo",
        city: "Colombo",
        country: "Sri Lanka"
      }

      window.payhere.startPayment(payment)
    } catch (error) {
      console.log("Payment error:", error)
    }
  }

  return (
    <div>
      <Navbar/>
      <div className="max-w-5xl mx-auto p-10">
        <h2 className="text-2xl mb-6">Checkout</h2>
        <button
          onClick={pay}
          className="bg-blue-600 px-8 py-3 rounded"
        >
          Pay Now
        </button>
      </div>
    </div>
  )
}