import {BrowserRouter, Routes, Route} from "react-router-dom";
import CreateAccount from "./component/localAuth/CreateAccount";
import Login from "./component/localAuth/Login";
import Home from "./component/home/Home"
import Marketplace from "./component/market/Marketplace.tsx";
import Reward from "./component/reward/Reward.tsx";
import KineticVault from "./component/landpage/KineticVault.tsx"
import EmailVerification from "./component/localAuth/EmailVerification.tsx"
import ResetPassword from  "./component/localAuth/ResetPassword.tsx"

export default function Router() {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/" element={<KineticVault/>}/>

                <Route path="/login" element={<Login/>}/>
                <Route path="/register" element={<CreateAccount/>}/>
                <Route path="/forgot-password" element={<EmailVerification/>}/>
                <Route path="/reset-password" element={<ResetPassword/>}/>

                <Route path="/inventory" element={<Home/>}/>
                <Route path="/market" element={<Marketplace/>}/>
                <Route path="/reward" element={<Reward/>}/>


            </Routes>
        </BrowserRouter>
    );
}
