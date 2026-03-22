import {BrowserRouter, Routes, Route} from "react-router-dom";
import CreateAccount from "./component/localAuth/CreateAccount";
import Login from "./component/localAuth/Login";
import Home from "./component/home/Home"
import Marketplace from "./component/market/Marketplace.tsx";
import Reward from "./component/reward/Reward.tsx";
import KineticVault from "./component/landpage/KineticVault.tsx"

export default function Router() {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/" element={<KineticVault/>}/>

                <Route path="/signin" element={<Login/>}/>
                <Route path="/signup" element={<CreateAccount/>}/>
                <Route path="/inventory" element={<Home/>}/>
                <Route path="/market" element={<Marketplace/>}/>
                <Route path="/reward" element={<Reward/>}/>

            </Routes>
        </BrowserRouter>
    );
}
