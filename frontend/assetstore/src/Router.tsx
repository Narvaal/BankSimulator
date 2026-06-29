import {BrowserRouter, Routes, Route} from "react-router-dom";
import {useEffect} from "react";
import CreateAccount from "./component/localAuth/CreateAccount";
import Login from "./component/localAuth/Login";
import Home from "./component/home/Home"
import Marketplace from "./component/market/Marketplace.tsx";
import Reward from "./component/reward/Reward.tsx";
import Logs from "./component/logs/Logs.tsx";
import ArtifactDetail from "./component/artifact/ArtifactDetail.tsx";
import RareLines from "./component/landpage/RareLines.tsx"
import EmailVerification from "./component/localAuth/EmailVerification.tsx"
import ResetPassword from  "./component/localAuth/ResetPassword.tsx"
import ProfilePage from "./component/profile/ProfilePage.tsx";
import SearchPage from "./component/search/SearchPage.tsx";
import {initSession} from "./auth.ts";

export default function Router() {
    useEffect(() => { initSession(); }, []);

    return (
        <BrowserRouter>
            <Routes>

                {/* Public */}
                <Route path="/" element={<RareLines/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/register" element={<CreateAccount/>}/>
                <Route path="/forgot-password" element={<EmailVerification/>}/>
                <Route path="/reset-password" element={<ResetPassword/>}/>

                {/* Public — profiles & search */}
                <Route path="/search" element={<SearchPage/>}/>
                <Route path="/profile/:accountId" element={<ProfilePage/>}/>

                {/* Private */}
                <Route path="/inventory" element={<Home/>}/>
                <Route path="/market" element={<Marketplace/>}/>
                <Route path="/reward" element={<Reward/>}/>
                <Route path="/logs" element={<Logs/>}/>
                <Route path="/artifact/:id" element={<ArtifactDetail/>}/>

            </Routes>
        </BrowserRouter>
    );
}
