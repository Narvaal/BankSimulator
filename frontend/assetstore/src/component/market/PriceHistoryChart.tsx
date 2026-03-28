import {Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";
import {useMemo} from "react";

interface AssetPriceHistory {
    assetId: number;
    assetUnityId: number;
    oldPrice: number;
    newPrice: number;
    createdAt: string;
}

interface Props {
    priceHistory?: AssetPriceHistory[];
}

export default function PriceHistoryChart({priceHistory = []}: Props) {

    const chartData = useMemo(() => {
        return [...priceHistory]
            .sort(
                (a, b) =>
                    new Date(a.createdAt).getTime() -
                    new Date(b.createdAt).getTime()
            )
            .map((h) => ({
                price: Number(h.newPrice),
                label: new Date(h.createdAt).toLocaleTimeString([], {
                    hour: "2-digit",
                    minute: "2-digit"
                })
            }));
    }, [priceHistory]);

    return (
        <div
            className="h-56 w-full p-3 select-none"
            onMouseDown={(e) => e.preventDefault()}
        >
            {chartData.length === 0 ? (

                <p className="text-sm text-slate-400 text-center mt-20">
                    No price history
                </p>

            ) : (

                <ResponsiveContainer width="100%" height="100%">
                    <AreaChart
                        data={chartData}
                        margin={{top: 10, right: 10, left: 0, bottom: 0}}
                    >

                        <defs>
                            <linearGradient id="priceGradient" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor="#10B981" stopOpacity={0.35}/>
                                <stop offset="95%" stopColor="#10B981" stopOpacity={0}/>
                            </linearGradient>
                        </defs>

                        <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="#e2e8f0"
                        />

                        <XAxis
                            dataKey="label"
                            tick={{fontSize: 11, fill: "#64748b"}}
                            tickLine={false}
                            axisLine={false}
                        />

                        <YAxis
                            tick={{fontSize: 11, fill: "#64748b"}}
                            tickFormatter={(v) => `$${v}`}
                            width={40}
                            tickLine={false}
                            axisLine={false}
                        />

                        <Tooltip
                            cursor={false}
                            contentStyle={{
                                background: "#ffffff",
                                borderRadius: "10px",
                                border: "1px solid #e2e8f0",
                                fontSize: "12px"
                            }}
                            formatter={(value) => {
                                const v = Number(value);
                                return [`$${v.toFixed(2)}`, "Price"];
                            }}
                        />

                        <Area
                            type="monotone"
                            dataKey="price"
                            stroke="#10B981"
                            strokeWidth={3}
                            fillOpacity={1}
                            fill="url(#priceGradient)"
                            dot={chartData.length === 1 ? {r: 5} : false}
                            activeDot={{r: 6}}
                        />

                    </AreaChart>
                </ResponsiveContainer>

            )}
        </div>
    );
}