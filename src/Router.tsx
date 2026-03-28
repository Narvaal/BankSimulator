import {BrowserRouter, Routes, Route} from "react-router-dom";
import CreateAccount from "./component/localAuth/CreateAccount";
import Login from "./component/localAuth/Login";
import Home from "./component/home/Home"
import Marketplace from "./component/market/Marketplace.tsx";
import Reward from "./component/reward/Reward.tsx";
import RareLines from "./component/landpage/RareLines.tsx"
import EmailVerification from "./component/localAuth/EmailVerification.tsx"
import ResetPassword from  "./component/localAuth/ResetPassword.tsx"

export default function Router() {
    return (
        <BrowserRouter>
            <Routes>

                {/* Public */}
                <Route path="/" element={<RareLines/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/register" element={<CreateAccount/>}/>
                <Route path="/forgot-password" element={<EmailVerification/>}/>
                <Route path="/reset-password" element={<ResetPassword/>}/>

                {/* Private */}
                <Route path="/inventory" element={<Home/>}/>
                <Route path="/market" element={<Marketplace/>}/>
                <Route path="/reward" element={<Reward/>}/>

            </Routes>
        </BrowserRouter>
    );
}
