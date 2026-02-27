import { motion, AnimatePresence } from "framer-motion";
import type { ComponentType, SVGProps, Dispatch, SetStateAction } from "react";

import {
    BriefcaseIcon,
    ShoppingCartIcon,
} from "@heroicons/react/24/outline";
import { ArrowLeftIcon } from "@heroicons/react/16/solid";

type IconType = ComponentType<SVGProps<SVGSVGElement>>;

interface NavItemProps {
    icon: IconType;
    text: string;
    collapsed: boolean;
    link: string;
}

function NavItem({ icon: Icon, text, collapsed, link }: NavItemProps) {
    return (
        <a href={link}>
            <div className="relative group">
                <div className="flex items-center h-11 rounded-lg px-3 cursor-pointer transition
                    border border-transparent hover:bg-slate-100">

                    <div className="w-10 flex justify-center shrink-0">
                        <Icon className="h-5 w-5 text-slate-600" />
                    </div>

                    <AnimatePresence>
                        {!collapsed && (
                            <motion.span
                                initial={{ opacity: 0, x: -6 }}
                                animate={{ opacity: 1, x: 0 }}
                                exit={{ opacity: 0, x: -6 }}
                                transition={{ duration: 0.12 }}
                                className="whitespace-nowrap text-sm text-slate-800"
                            >
                                {text}
                            </motion.span>
                        )}
                    </AnimatePresence>
                </div>

                {collapsed && (
                    <div
                        className="absolute left-full top-1/2 -translate-y-1/2 ml-3
                        rounded-md bg-slate-900 text-slate-100 text-xs px-2 py-1
                        opacity-0 group-hover:opacity-100 transition
                        pointer-events-none whitespace-nowrap z-50 shadow-lg"
                    >
                        {text}
                    </div>
                )}
            </div>
        </a>
    );
}

interface Props {
    collapsed: boolean;
    setCollapsed: Dispatch<SetStateAction<boolean>>;
}

export default function NavBar({ collapsed, setCollapsed }: Props) {

    const sidebarWidth = collapsed ? 80 : 256;

    return (
        <>
            <motion.aside
                className="fixed left-0 top-0 h-screen border-r border-slate-200
                bg-white/70 backdrop-blur-md"
                animate={{ width: sidebarWidth }}
                transition={{ type: "spring", stiffness: 220, damping: 26 }}
            >
                <div className="h-14 flex items-center px-4 font-medium text-slate-800">
                    <AnimatePresence>
                        {!collapsed && (
                            <motion.span
                                initial={{ opacity: 0, x: -6 }}
                                animate={{ opacity: 1, x: 0 }}
                                exit={{ opacity: 0, x: -6 }}
                                transition={{ duration: 0.12 }}
                                className="whitespace-nowrap"
                            >
                                Dashboard
                            </motion.span>
                        )}
                    </AnimatePresence>
                </div>

                <hr className="border-slate-200" />

                <nav className="mt-4 px-2 space-y-1">
                    <NavItem icon={BriefcaseIcon} text="Inventory" collapsed={collapsed} link="/"/>
                    <NavItem icon={ShoppingCartIcon} text="Marketplace" collapsed={collapsed} link="/market"/>
                </nav>

                <AnimatePresence>
                    {!collapsed && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                            transition={{ duration: 0.2 }}
                            className="absolute bottom-0 left-0 w-full p-4 border-t border-slate-200 text-xs text-slate-500 overflow-hidden"
                        >
                            <div className="whitespace-nowrap min-w-max">
                                Logged as <span className="text-slate-800 font-medium">Alessandro</span>
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </motion.aside>

            <motion.button
                onClick={() => setCollapsed(v => !v)}
                className="fixed top-3 z-50 w-8 h-8 flex items-center justify-center rounded-md
                border border-slate-200 bg-white/80 backdrop-blur hover:bg-slate-100"
                animate={{ left: collapsed ? 25 : 210 }}
                transition={{ type: "spring", stiffness: 260, damping: 24 }}
            >
                <motion.div
                    animate={{ rotate: collapsed ? 180 : 0 }}
                    transition={{ type: "spring", stiffness: 260, damping: 20 }}
                >
                    <ArrowLeftIcon className="h-5 w-5 text-slate-700" />
                </motion.div>
            </motion.button>
        </>
    );
}




