import React, {useEffect, useState} from "react";
import './style/Navbar.css'

const Navbar = () => {

    const [userName, setUserName] = useState(localStorage.getItem("username") || "Gość");

    useEffect(() => {
        const intervalId = setInterval(() => { // Przechowuje identyfikator dla późniejszego wyczyszczenia
            const currentUserName = localStorage.getItem("username");
            if (userName !== currentUserName) {
                setUserName(currentUserName || "Gość");
            }
        }, 1000); // Sprawdza co sekundę

        return () => clearInterval(intervalId); // Czyści timer przy odmontowaniu komponentu
    }, [userName]);

    const isLoggedIn = !!localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        setUserName("Gość");
        window.location.reload(); // Przeładowuje stronę aby zaktualizować stan aplikacji
    };

    return (
        <nav>
            <div>
                Zalogowano jako: {userName}
            </div>
            <div>
                {isLoggedIn && (
                    <button onClick={handleLogout}>
                        Wyloguj
                    </button>
                )}
            </div>
            <div>
                <a href="/login">Zaloguj</a>
                <a href="/register">Zarejestruj</a>
            </div>
        </nav>
    );
};

export default Navbar;
