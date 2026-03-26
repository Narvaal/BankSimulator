import React, { useEffect } from "react";

const KineticVault: React.FC = () => {
  useEffect(() => {
    const linkFonts = document.createElement("link");
    linkFonts.href =
      "https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&family=Space+Grotesk:wght@400;600;700&display=swap";
    linkFonts.rel = "stylesheet";

    const linkIcons = document.createElement("link");
    linkIcons.href =
      "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined";
    linkIcons.rel = "stylesheet";

    document.head.appendChild(linkFonts);
    document.head.appendChild(linkIcons);

    const style = document.createElement("style");
    style.innerHTML = `
      body {
        font-family: 'Inter', sans-serif;
      }

      * {
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
      }

      .material-symbols-outlined {
        font-variation-settings:
          'FILL' 0,
          'wght' 400,
          'GRAD' 0,
          'opsz' 24;
      }

      @keyframes fadeIn {
        from {
          opacity: 0;
          transform: translateY(10px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }
    `;
    document.head.appendChild(style);

    return () => {
      document.head.removeChild(linkFonts);
      document.head.removeChild(linkIcons);
      document.head.removeChild(style);
    };
  }, []);

  return (
    <div className="bg-[#0b0f14] text-white min-h-screen relative overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(173,198,255,0.08),transparent_40%),radial-gradient(circle_at_80%_0%,rgba(111,221,120,0.06),transparent_40%)]"></div>

      <header className="sticky top-0 z-50 bg-[#181c22]/80 backdrop-blur-md border-b border-white/5">
        <nav className="flex justify-between items-center px-6 py-3 max-w-7xl mx-auto">
          <div className="flex items-center gap-8">
            <h1 className="text-lg font-semibold tracking-tight text-white">
              <span className="text-[#adc6ff]">Kinetic</span> Vault
            </h1>

            <div className="hidden md:flex gap-6 text-sm">
              <a href="#build" className="text-gray-500 hover:text-white transition">Build</a>
              <a href="#stack" className="text-gray-500 hover:text-white transition">Stack</a>
            </div>
          </div>

        <div className="flex items-center gap-3">

          <a
            href="https://github.com/Narvaal"
            target="_blank"
            rel="noopener noreferrer"
            className="group flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium
                       bg-[#0f131a]/70 backdrop-blur border border-white/10
                       text-gray-300 hover:text-white
                       hover:border-[#adc6ff]/40 hover:-translate-y-[1px]
                       transition-all duration-300"
          >
            <svg
              className="w-4 h-4 opacity-80 group-hover:opacity-100"
              viewBox="0 0 24 24"
              fill="currentColor"
            >
              <path d="M12 .5C5.65.5.5 5.65.5 12c0 5.09 3.29 9.4 7.86 10.93.58.1.79-.25.79-.56v-2.17c-3.2.7-3.88-1.54-3.88-1.54-.53-1.34-1.3-1.7-1.3-1.7-1.06-.72.08-.71.08-.71 1.17.08 1.79 1.2 1.79 1.2 1.04 1.77 2.74 1.26 3.41.96.1-.75.41-1.26.74-1.55-2.55-.29-5.24-1.27-5.24-5.64 0-1.25.45-2.28 1.19-3.09-.12-.29-.52-1.46.11-3.05 0 0 .97-.31 3.17 1.18a11.06 11.06 0 0 1 5.78 0c2.2-1.49 3.17-1.18 3.17-1.18.63 1.59.23 2.76.11 3.05.74.81 1.19 1.84 1.19 3.09 0 4.38-2.69 5.35-5.25 5.63.42.36.79 1.09.79 2.2v3.27c0 .31.21.66.8.55A10.99 10.99 0 0 0 23.5 12C23.5 5.65 18.35.5 12 .5z" />
            </svg>

            GitHub
          </a>

          <a
            href="https://www.linkedin.com/in/seu-link-aqui/"
            target="_blank"
            rel="noopener noreferrer"
            className="group flex items-center gap-2 px-5 py-2 rounded-lg text-sm font-semibold
                       text-black bg-[#adc6ff]
                       hover:-translate-y-[1px]
                       transition-all duration-300 overflow-hidden"
          >
            <span className="flex items-center gap-2">
              <svg
                className="w-4 h-4"
                viewBox="0 0 24 24"
                fill="currentColor"
              >
                <path d="M20.45 20.45h-3.56v-5.6c0-1.33-.03-3.04-1.85-3.04-1.85 0-2.13 1.45-2.13 2.94v5.7H9.35V9h3.42v1.56h.05c.48-.9 1.64-1.85 3.38-1.85 3.62 0 4.29 2.38 4.29 5.48v6.26zM5.34 7.43a2.06 2.06 0 1 1 0-4.12 2.06 2.06 0 0 1 0 4.12zM7.12 20.45H3.56V9h3.56v11.45z" />
              </svg>

              LinkedIn
            </span>
          </a>

        </div>
        </nav>
      </header>

      <section className="max-w-7xl mx-auto px-6 py-28 grid lg:grid-cols-12 gap-12 items-center">
        <div className="lg:col-span-7">
          <span className="text-green-400 text-xs uppercase tracking-[0.2em] mb-6 block">
            Full-Stack System Design
          </span>

          <h1 className="text-6xl md:text-7xl font-extrabold leading-[1.05] mb-6 tracking-tight">
            Mastering <br />
            <span className="bg-gradient-to-r from-[#adc6ff] to-[#6fdd78] bg-clip-text text-transparent">
              Distributed Systems
            </span>
          </h1>

          <p className="text-gray-400 text-lg mb-10 max-w-xl leading-relaxed">
            Built from scratch to explore real-world backend scalability using AWS, Spring Boot and React.
          </p>

            <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">

              <a
                href="https://app.alessandro-bezerra.me/login/"
                target="_blank"
                rel="noopener noreferrer"
                className="group relative inline-flex items-center gap-2 px-6 py-3 rounded-lg font-semibold text-black bg-[#adc6ff] overflow-hidden transition-all duration-300 hover:-translate-y-1"
              >
                <span className="relative z-10 flex items-center gap-2">
                  Try it
                  <span className="material-symbols-outlined text-sm transition group-hover:translate-x-1">
                    arrow_forward
                  </span>
                </span>

                <span className="absolute inset-0 bg-gradient-to-r from-white/20 to-transparent opacity-0 group-hover:opacity-100 transition"></span>
              </a>

              <a
                href="https://github.com/Narvaal/BankSimulator"
                target="_blank"
                rel="noopener noreferrer"
                className="group inline-flex items-center gap-2 px-4 py-3 text-gray-400 hover:text-white transition"
              >
                <span className="material-symbols-outlined text-lg">
                  code
                </span>

                <span className="relative">
                  View Code
                  <span className="absolute left-0 -bottom-1 w-0 h-[1px] bg-white transition-all duration-300 group-hover:w-full"></span>
                </span>
              </a>

            </div>
        </div>

        <div className="lg:col-span-5">
          <div className="bg-[#0a0e14]/80 backdrop-blur rounded-2xl p-6 border border-white/10 shadow-[0_20px_60px_rgba(0,0,0,0.6)] hover:scale-[1.01] transition">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-3 h-3 bg-red-400 rounded-full" />
              <div className="w-3 h-3 bg-yellow-400 rounded-full" />
              <div className="w-3 h-3 bg-green-400 rounded-full" />
            </div>

            <pre className="text-sm font-mono leading-relaxed overflow-x-auto">
              <code>
                <span className="text-purple-400">@Service</span>
                {"\n"}
                <span className="text-blue-400">public class</span>{" "}
                <span className="text-white">VaultService</span> {"{"}
                {"\n\n"}
                {"  "}
                <span className="text-purple-400">@Autowired</span>
                {"\n  "}
                <span className="text-blue-400">private</span>{" "}
                <span className="text-white">AssetRepository</span>{" "}
                <span className="text-gray-300">repo;</span>
                {"\n\n"}
                {"  "}
                <span className="text-blue-400">public</span>{" "}
                <span className="text-white">Asset</span>{" "}
                <span className="text-green-400">processOwnership</span>
                (User u) {"{"}
                {"\n    "}
                <span className="text-blue-400">if</span>{" "}
                <span className="text-gray-300">(!u.isVerified())</span> {"{"}
                {"\n      "}
                <span className="text-blue-400">throw new</span>{" "}
                <span className="text-red-400">SecurityException</span>();
                {"\n    }"}
                {"\n    "}
                <span className="text-blue-400">return</span>{" "}
                <span className="text-gray-300">repo.save(new Asset(u));</span>
                {"\n  }"}
                {"\n"}
                {"}"}
              </code>
            </pre>
          </div>
        </div>
      </section>

      <section id="build" className="max-w-7xl mx-auto px-6 py-32">
        <div className="bg-[#12161d]/80 backdrop-blur rounded-3xl p-12 lg:p-20 border border-white/10 relative overflow-hidden">
          <div className="absolute -top-20 -right-20 w-72 h-72 bg-[#adc6ff]/10 blur-[120px] rounded-full"></div>

          <div className="mb-16 max-w-3xl">
            <span className="text-[#6fdd78] text-xs uppercase tracking-[0.25em] block mb-4">
              System Overview
            </span>

            <h2 className="text-4xl md:text-5xl font-extrabold tracking-tight mb-6">
              A Secure and Distributed Asset Marketplace
            </h2>

            <p className="text-gray-400 text-lg leading-relaxed">
              Kinetic Vault is a distributed asset management and trading system engineered
              to enforce strict ownership integrity and transactional reliability.
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-10">
            {[
              {
                title: "Ownership & Consistency",
                desc: "Each asset is modeled as an individual unit, ensuring atomic and traceable operations."
              },
              {
                title: "Marketplace Engine",
                desc: "Real-time trading, pricing, and full historical tracking."
              },
              {
                title: "Security First Design",
                desc: "Transactions are cryptographically validated and verified."
              },
              {
                title: "Scalable Architecture",
                desc: "Stateless backend optimized for distributed environments."
              }
            ].map((item) => (
              <div key={item.title} className="hover:-translate-y-1 transition">
                <h3 className="text-white font-semibold mb-3">{item.title}</h3>
                <p className="text-gray-400">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
        <section
          id="stack"
          className="relative py-32 bg-[#12161d] border-y border-white/5 overflow-hidden"
        >
          {/* background glow */}
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_0%,rgba(173,198,255,0.08),transparent_60%)]"></div>

          <div className="max-w-7xl mx-auto px-6 relative">

            {/* header */}
            <div className="text-center mb-20 max-w-2xl mx-auto">
              <span className="text-[#adc6ff] text-xs uppercase tracking-[0.3em] block mb-4">
                Architecture
              </span>

              <h2 className="text-4xl md:text-5xl font-extrabold tracking-tight mb-6">
                System Architecture
              </h2>

              <p className="text-gray-400 text-lg">
                Built with a modern full-stack architecture focused on performance,
                scalability and security.
              </p>
            </div>

            {/* cards */}
            <div className="grid md:grid-cols-3 gap-8">
              {[
                {
                  title: "Backend",
                  items: ["Spring Boot", "JWT Auth", "PostgreSQL", "Hibernate"],
                },
                {
                  title: "Frontend",
                  items: ["React", "Tailwind", "Context API", "Responsive UX"],
                },
                {
                  title: "Infrastructure",
                  items: ["AWS EC2", "S3", "VPC", "CI/CD"],
                },
              ].map((section) => (
                <div
                  key={section.title}
                  className="group relative p-[1px] rounded-2xl bg-gradient-to-b from-white/10 to-transparent"
                >
                  <div className="h-full bg-[#0f131a]/80 backdrop-blur rounded-2xl p-8 border border-white/5 transition-all duration-300 group-hover:border-[#adc6ff]/40 group-hover:-translate-y-2 group-hover:shadow-[0_20px_60px_rgba(0,0,0,0.6)]">

                    <h3 className="uppercase text-xs font-semibold tracking-[0.2em] mb-6 text-[#adc6ff]">
                      {section.title}
                    </h3>

                    <ul className="space-y-3 text-gray-400 text-sm">
                      {section.items.map((item) => (
                        <li
                          key={item}
                          className="flex items-center gap-2 group-hover:text-gray-300 transition"
                        >
                          <span className="w-1.5 h-1.5 bg-[#adc6ff] rounded-full"></span>
                          {item}
                        </li>
                      ))}
                    </ul>

                  </div>
                </div>
              ))}
            </div>

          </div>
        </section>

      <footer className="border-t border-white/5 px-6 py-4 flex justify-between text-xs text-gray-500">
        <span>KINETIC_VAULT.dev</span>
        <span className="text-green-400">System: Stable v2.4.0</span>
      </footer>
    </div>
  );
};

export default KineticVault;