import {useEffect, useState} from "react";

type CountdownTimerProps = {
    targetDate: string | Date;
};

export default function CountdownTimer({targetDate}: CountdownTimerProps) {
    const calculateTimeLeft = () => {
        const difference =
            new Date(targetDate).getTime() - new Date().getTime();

        if (difference <= 0) {
            return {finished: true, time: "00:00"};
        }

        const hours = Math.floor(difference / (1000 * 60 * 60));
        const minutes = Math.floor((difference / (1000 * 60)) % 60);
        const seconds = Math.floor((difference / 1000) % 60);

        const formatted = `${String(hours).padStart(2, "0")}:${String(
            minutes
        ).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;

        return {finished: false, time: formatted};
    };

    const [state, setState] = useState(calculateTimeLeft());

    useEffect(() => {
        const interval = setInterval(() => {
            setState(calculateTimeLeft());
        }, 1000);

        return () => clearInterval(interval);
    }, [targetDate]);

    return (
        <div
            className="flex flex-col items-center justify-center w-full max-w-sm mx-auto">
            <span className="text-slate-800 text-sm">
                Next Free Asset
            </span>

            {state.finished ? (
                <span className="text-green-400 text-2xl font-bold">
                    Available Now!
                </span>
            ) : (
                <span className="text-xl font-mono font-bold text-slate-800 tracking-widest">
                    {state.time}
                </span>
            )}
        </div>
    );
}