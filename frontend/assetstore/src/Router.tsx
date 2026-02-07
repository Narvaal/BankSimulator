import {BrowserRouter, Routes, Route} from "react-router-dom";
import CreateAccount from "./component/localAuth/CreateAccount.tsx";
import Login from "./component//localAuth/Login";

export default function Router() {
    return (
        <BrowserRouter>
            <Routes>

                {/* PUBLIC */}
                <Route path="/singin" element={<Login/>}/>
                <Route path="/signup" element={<CreateAccount/>}/>

            </Routes>
        </BrowserRouter>
    );
}
