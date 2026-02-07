import type {ReactNode} from "react";

type AuthLayoutProps = {
    title: string;
    description?: string;
    children: ReactNode;
};

function AuthLayout({ title, description, children }: AuthLayoutProps) {
    return (
        <div className="flex justify-center items-center min-h-screen from-gray-100 to-gray-300">
            <div className="w-full max-w-sm rounded-2xl bg-white/80 backdrop-blur p-8 shadow-2xl border border-gray-200">
                <h1 className="text-2xl font-semibold text-center mb-6">
                    {title}
                </h1>
                {description && (
                    <p className="text-sm text-gray-500 text-center mb-6">
                        {description}
                    </p>
                )}
                {children}
            </div>
        </div>
    );
}

export default AuthLayout;
