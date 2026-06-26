import {AnimatePresence, motion} from "framer-motion";
import type {ComponentType, Dispatch, SetStateAction, SVGProps} from "react";
import {BriefcaseIcon, GiftIcon, ShoppingCartIcon} from "@heroicons/react/24/outline";
import {ChevronLeftIcon} from "@heroicons/react/20/solid";
import {Link, useLocation} from "react-router-dom";

type IconType = ComponentType<SVGProps<SVGSVGElement>>;

interface NavItemProps {
    icon: IconType;
    text: string;
    collapsed: boolean;
    link: string;
}

function NavItem({icon: Icon, text, collapsed, link}: NavItemProps) {
    const location = useLocation();
    const active = location.pathname === link;

    return (
        <Link to={link}>
            <div className="relative group">
                <div className={`flex items-center h-8 rounded-md px-2.5 gap-2 cursor-pointer transition-colors
                    ${active
                        ? "bg-zinc-900 text-white"
                        : "text-zinc-500 hover:text-zinc-900 hover:bg-zinc-100"
                    }`}
                >
                    <div className="w-4 flex justify-center shrink-0">
                        <Icon className="h-4 w-4"/>
                    </div>

                    <AnimatePresence>
                        {!collapsed && (
                            <motion.span
                                initial={false}
                                animate={{opacity: 1, x: 0}}
                                exit={{opacity: 0, x: -4}}
                                transition={{duration: 0.1}}
                                className="whitespace-nowrap text-xs font-medium"
                            >
                                {text}
                            </motion.span>
                        )}
                    </AnimatePresence>
                </div>

                {collapsed && (
                    <div className="absolute left-full top-1/2 -translate-y-1/2 ml-2 rounded-md bg-zinc-900 text-white text-xs px-2 py-1 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-50 shadow-lg">
                        {text}
                    </div>
                )}
            </div>
        </Link>
    );
}

interface Props {
    collapsed: boolean;
    setCollapsed: Dispatch<SetStateAction<boolean>>;
}

export default function Sidebar({collapsed, setCollapsed}: Props) {
    const sidebarWidth = collapsed ? 56 : 200;

    return (
        <motion.aside
            className="fixed left-0 top-12 h-[calc(100vh-3rem)] z-30 border-r border-zinc-100 bg-white flex flex-col"
            animate={{width: sidebarWidth}}
            initial={false}
            transition={{type: "spring", stiffness: 280, damping: 28}}
        >
            <nav className="flex-1 p-2 pt-3 space-y-0.5">
                <NavItem icon={BriefcaseIcon} text="Inventory" collapsed={collapsed} link="/inventory"/>
                <NavItem icon={ShoppingCartIcon} text="Marketplace" collapsed={collapsed} link="/market"/>
                <NavItem icon={GiftIcon} text="Rewards" collapsed={collapsed} link="/reward"/>
            </nav>

            <div className="p-2 border-t border-zinc-100">
                <button
                    onClick={() => setCollapsed(v => !v)}
                    className={`flex items-center h-8 rounded-md px-2.5 gap-2 w-full text-zinc-400 hover:text-zinc-700 hover:bg-zinc-100 transition-colors ${collapsed ? "justify-center" : ""}`}
                >
                    <div className="w-4 flex justify-center shrink-0">
                        <motion.div
                            animate={{rotate: collapsed ? 180 : 0}}
                            transition={{type: "spring", stiffness: 280, damping: 24}}
                        >
                            <ChevronLeftIcon className="h-3.5 w-3.5"/>
                        </motion.div>
                    </div>

                    <AnimatePresence>
                        {!collapsed && (
                            <motion.span
                                initial={false}
                                animate={{opacity: 1, x: 0}}
                                exit={{opacity: 0, x: -4}}
                                transition={{duration: 0.1}}
                                className="whitespace-nowrap text-xs text-zinc-400"
                            >
                                Collapse
                            </motion.span>
                        )}
                    </AnimatePresence>
                </button>
            </div>
        </motion.aside>
    );
}
