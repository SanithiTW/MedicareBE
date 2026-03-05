import React, { useState, useRef } from 'react';
import Slider from 'react-slick';
import { motion, useInView } from 'framer-motion';
import CountUp from 'react-countup';
import { useNavigate } from 'react-router-dom';
import './LandingPage.css';



// Assets
import Logo from '../assets/GetSeat_logo.jpg';
import busIcon from '../assets/bus.png';
import seatIcon from '../assets/seat.jpg';
import ticketIcon from '../assets/ticket.jpg';
import cityIcon from '../assets/city.png';

// Slick carousel CSS
import "slick-carousel/slick/slick.css"; 
import "slick-carousel/slick/slick-theme.css"; 

const features = [
    { title: "Fast Booking", icon: ticketIcon },
    { title: "Choose Your Seat", icon: seatIcon },
    { title: "Multiple Routes", icon: cityIcon },
    { title: "Real-time Availability", icon: busIcon },
];

const LandingPage = () => {
    const navigate = useNavigate();
    const [activeSearchTab, setActiveSearchTab] = useState('oneWay');
    const searchRef = useRef(null);

    const sliderSettings = {
        dots: false,
        infinite: true,
        speed: 1200,
        slidesToShow: 1,
        slidesToScroll: 1,
        autoplay: true,
        autoplaySpeed: 5000,
        fade: true,
        cssEase: "ease-in-out"
    };

    const AnimatedCounter = ({ endValue, label }) => {
        const ref = useRef(null);
        const isInView = useInView(ref, { once: true, amount: 0.5 });
        return (
            <div className="counter-item" ref={ref}>
                <span className="counter-number">{isInView ? <CountUp end={endValue} duration={2.5} /> : 0}+</span>
                <p className="counter-label">{label}</p>
            </div>
        );
    };

    return (
        <div className="landing-page">
            {/* Header */}
            <header className="header">
                <div className="logo-container">
                    <img src={Logo} alt="GetSeat Logo" className="logo" />
                </div>
                <nav className="nav-links">
                    <a href="#features">Features</a>
                    <a href="#search">Book Now</a>
                    <a href="#stats">Stats</a>
                    <a href="#footer">Contact</a>
                </nav>
                <div className="auth-buttons">
                    <button onClick={() => navigate('/login')}>Sign In</button>
                    <button onClick={() => navigate('/register')}>Register</button>
                </div>
            </header>
      <section className="flex flex-col items-center justify-center text-center py-48 bg-gradient-to-br from-blue-900 to-blue-600 relative overflow-hidden">
        {/* Floating shapes */}
        <motion.div
          className="absolute w-32 h-32 bg-blue-400 rounded-full opacity-20 top-10 left-10"
          animate={{ y: [0, 20, 0] }}
          transition={{ duration: 5, repeat: Infinity }}
        />
        <motion.div
          className="absolute w-48 h-48 bg-blue-300 rounded-full opacity-15 bottom-10 right-20"
          animate={{ y: [0, -20, 0] }}
          transition={{ duration: 6, repeat: Infinity }}
        />

        <h1 className="text-5xl md:text-6xl font-bold text-white mb-4 z-10">
          Travel Smarter with GetSeat
        </h1>
        <p className="text-gray-200 text-lg mb-6 z-10">
          Book your bus seats online, choose your route, and enjoy a comfortable journey with real-time seat availability.
        </p>
        <button 
          onClick={() => searchRef.current.scrollIntoView({ behavior: 'smooth' })} 
          className="px-8 py-3 bg-blue-400 hover:bg-blue-300 rounded-lg font-semibold z-10"
        >
          Start Booking
        </button>
      </section>

            {/* Features */}
            <section id="features" className="features-section">
                <h2>Why Choose GetSeat?</h2>
                <div className="features-grid">
                    {features.map((f, i) => (
                        <motion.div key={i} className="feature-card" whileHover={{ scale: 1.05, boxShadow: "0 8px 25px rgba(59,130,246,0.4)" }}>
                            <img src={f.icon} alt={f.title} />
                            <h3>{f.title}</h3>
                        </motion.div>
                    ))}
                </div>
            </section>

            {/* Search Section */}
            <section id="search" className="search-section" ref={searchRef}>
                <h2>Find Your Bus</h2>
                <div className="search-tabs">
                    <button className={activeSearchTab === 'oneWay' ? 'active' : ''} onClick={() => setActiveSearchTab('oneWay')}>One Way</button>
                    <button className={activeSearchTab === 'roundTrip' ? 'active' : ''} onClick={() => setActiveSearchTab('roundTrip')}>Round Trip</button>
                </div>
                <form className="search-form">
                    <input type="text" placeholder="From" />
                    <input type="text" placeholder="To" />
                    <input type="date" placeholder="Departure" />
                    {activeSearchTab === 'roundTrip' && <input type="date" placeholder="Return" />}
                    <button type="submit">Search Buses</button>
                </form>
            </section>

            {/* Stats */}
            <section id="stats" className="stats-section">
                <AnimatedCounter endValue={120} label="Routes" />
                <AnimatedCounter endValue={500} label="Buses" />
                <AnimatedCounter endValue={15000} label="Passengers Served" />
                <AnimatedCounter endValue={50} label="Cities Covered" />
            </section>

            {/* Footer */}
            <footer id="footer" className="footer">
                <div className="footer-content">
                    <img src={Logo} alt="GetSeat Logo" className="footer-logo"/>
                    <div className="footer-links">
                        <a href="#">About</a>
                        <a href="#">Services</a>
                        <a href="#">FAQ</a>
                        <a href="#">Contact</a>
                    </div>
                    <p>© 2026 GetSeat. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
};

export default LandingPage;