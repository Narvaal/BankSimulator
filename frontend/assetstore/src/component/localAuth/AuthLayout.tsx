import { ReactNode } from "react";

type AuthLayoutProps = {
  title: string;
  description?: string;
  children: ReactNode;
};

function AuthLayout({ title, description, children }: AuthLayoutProps) {
  return (
    <div className="relative flex justify-center items-center min-h-screen overflow-hidden bg-gradient-to-br from-gray-50 to-gray-200">

      <div className="absolute inset-0 overflow-hidden">

        <div
          className="absolute w-[700px] h-[700px] bg-[#adc6ff]/40 rounded-full blur-[120px]"
          style={{ top: "5%", left: "5%", animation: "blobA 28s infinite linear" }}
        />

        <div
          className="absolute w-[600px] h-[600px] bg-[#4ADE80]/30 rounded-full blur-[120px]"
          style={{ top: "60%", left: "70%", animation: "blobB 24s infinite linear" }}
        />

        <div
          className="absolute w-[500px] h-[500px] bg-purple-300/30 rounded-full blur-[100px]"
          style={{ top: "30%", left: "40%", animation: "blobC 32s infinite linear" }}
        />

        <div
          className="absolute w-[450px] h-[450px] bg-pink-300/20 rounded-full blur-[100px]"
          style={{ top: "10%", left: "75%", animation: "blobD 36s infinite linear" }}
        />

        <div
          className="absolute w-[400px] h-[400px] bg-cyan-300/20 rounded-full blur-[100px]"
          style={{ top: "70%", left: "15%", animation: "blobE 30s infinite linear" }}
        />

      </div>

      <div className="relative w-full max-w-sm rounded-2xl bg-white/80 backdrop-blur-md p-8 shadow-xl border border-gray-200">
        <h1 className="text-2xl font-semibold text-center mb-4 text-gray-800">
          {title}
        </h1>

        {description && (
          <p className="text-sm text-gray-500 text-center mb-6">
            {description}
          </p>
        )}

        {children}
      </div>

      <style>
        {`
          @keyframes blobA {
            0%   { transform: translate(0, 0) scale(1); }
            25%  { transform: translate(300px, -200px) scale(1.2); }
            50%  { transform: translate(-250px, 250px) scale(0.9); }
            75%  { transform: translate(200px, 200px) scale(1.1); }
            100% { transform: translate(0, 0) scale(1); }
          }

          @keyframes blobB {
            0%   { transform: translate(0, 0) scale(1); }
            25%  { transform: translate(-300px, 200px) scale(1.25); }
            50%  { transform: translate(250px, -250px) scale(0.85); }
            75%  { transform: translate(-200px, -150px) scale(1.1); }
            100% { transform: translate(0, 0) scale(1); }
          }

          @keyframes blobC {
            0%   { transform: translate(0, 0) scale(1); }
            25%  { transform: translate(200px, 300px) scale(1.15); }
            50%  { transform: translate(-300px, -200px) scale(0.9); }
            75%  { transform: translate(150px, -150px) scale(1.05); }
            100% { transform: translate(0, 0) scale(1); }
          }

          @keyframes blobD {
            0%   { transform: translate(0, 0) scale(1); }
            50%  { transform: translate(-350px, 150px) scale(1.3); }
            100% { transform: translate(0, 0) scale(1); }
          }

          @keyframes blobE {
            0%   { transform: translate(0, 0) scale(1); }
            50%  { transform: translate(300px, -250px) scale(0.8); }
            100% { transform: translate(0, 0) scale(1); }
          }
        `}
      </style>
    </div>
  );
}

export default AuthLayout;