import {BrowserRouter, Routes, Route} from "react-router-dom";
import CreateAccount from "./component/localAuth/CreateAccount";
import Login from "./component/localAuth/Login";
import Home from "./component/home/Home"
import Marketplace from "./component/market/Marketplace.tsx";

export default function Router() {
    return (
        <BrowserRouter>
            <Routes>

                {/* PUBLIC */}
                <Route path="/singin" element={<Login/>}/>
                <Route path="/signup" element={<CreateAccount/>}/>

                <Route path="/" element={<Home/>}/>
                <Route path="/market" element={<Marketplace/>}/>

            </Routes>
        </BrowserRouter>
    );
}
