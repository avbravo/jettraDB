package io.jettra.ui.template;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private String title = "Jettra Application";
    private String content = "";
    private List<String> scripts = new ArrayList<>();
    private List<String> scriptContents = new ArrayList<>();
    private List<String> cssLinks = new ArrayList<>();

    public Page() {
        // Default dependencies
        addCss("https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600&display=swap");
        addScript("https://cdn.tailwindcss.com");

        // Configure tailwind and global styles
        addScriptContent(
                "tailwind.config = { darkMode: 'class', theme: { extend: { colors: { primary: {'50':'#eff6ff','100':'#dbeafe','200':'#bfdbfe','300':'#93c5fd','400':'#60a5fa','500':'#6366f1','600':'#4f46e5','700':'#4338ca','800':'#3730a3','900':'#312e81','950':'#1e1b4b'} } } } }");

        addScript("https://unpkg.com/htmx.org@2.0.4");
        addCss("https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.2.1/flowbite.min.css");

        // Global Futuristic styles
        addScriptContent(
                """
                            const style = document.createElement('style');
                            style.textContent = `
                                :root {
                                    --primary: #007bff;
                                    --primary-dark: #0056b3;
                                    --secondary: #00ffff;
                                    --bg: #050505;
                                    --glass: rgba(10, 10, 15, 0.8);
                                    --glass-border: rgba(0, 123, 255, 0.3);
                                    --text: #f8fafc;
                                    --glow-cyan: rgba(0, 255, 255, 0.5);
                                    --glow-blue: rgba(0, 123, 255, 0.5);
                                }
                                body {
                                    margin: 0;
                                    overflow-x: hidden;
                                    font-family: 'Outfit', sans-serif !important;
                                    background: #050505 !important;
                                    color: var(--text) !important;
                                    min-height: 100vh;
                                }

                                /* J3D Design System */
                                .j3d-card {
                                    background: var(--glass) !important;
                                    backdrop-filter: blur(15px);
                                    border: 1px solid var(--glass-border) !important;
                                    border-radius: 1.25rem;
                                    box-shadow: 0 0 20px var(--glow-blue) !important;
                                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                                }
                                .j3d-card:hover {
                                    transform: translateY(-4px) scale(1.01);
                                    box-shadow: 0 0 30px var(--glow-cyan) !important;
                                    border-color: var(--secondary) !important;
                                }

                                .j3d-button {
                                    background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important;
                                    color: white !important;
                                    border-radius: 0.75rem;
                                    border: 1px solid rgba(255, 255, 255, 0.1) !important;
                                    box-shadow: 0 0 15px var(--glow-blue);
                                    transition: all 0.2s ease;
                                    cursor: pointer;
                                }
                                .j3d-button:hover {
                                    filter: brightness(1.2);
                                    box-shadow: 0 0 20px var(--glow-cyan);
                                    transform: translateY(-1px);
                                }

                                .j3d-input, .j3d-select, .j3d-textarea {
                                    background: rgba(15, 15, 20, 0.6) !important;
                                    border: 1px solid var(--glass-border) !important;
                                    border-radius: 0.75rem !important;
                                    color: white !important;
                                    transition: all 0.2s ease;
                                }
                                .j3d-input:focus, .j3d-select:focus, .j3d-textarea:focus {
                                    border-color: var(--secondary) !important;
                                    box-shadow: 0 0 10px var(--glow-cyan) !important;
                                    outline: none !important;
                                }

                                .j3d-sidebar {
                                    background: rgba(10, 10, 15, 0.85) !important;
                                    backdrop-filter: blur(20px);
                                    border-right: 1px solid var(--glass-border) !important;
                                    box-shadow: 0 0 30px rgba(0, 0, 0, 0.5);
                                }

                                .j3d-navbar {
                                    background: rgba(5, 5, 10, 0.9) !important;
                                    backdrop-filter: blur(15px);
                                    border-bottom: 1px solid var(--glass-border) !important;
                                    box-shadow: 0 4px 30px rgba(0, 0, 0, 0.5);
                                }

                                .j3d-alert {
                                    backdrop-filter: blur(12px);
                                    border: 1px solid transparent;
                                    box-shadow: 0 0 15px var(--glow-blue);
                                }
                                .j3d-alert-info { background: rgba(0, 123, 255, 0.1) !important; color: #00ffff !important; border-color: var(--glass-border) !important; }
                                .j3d-alert-danger { background: rgba(239, 68, 68, 0.1) !important; color: #fca5a5 !important; border-color: rgba(239, 68, 68, 0.3) !important; }
                                .j3d-alert-success { background: rgba(0, 255, 136, 0.1) !important; color: #00ff88 !important; border-color: rgba(0, 255, 136, 0.3) !important; }
                                .j3d-alert-warning { background: rgba(245, 158, 11, 0.1) !important; color: #fde047 !important; border-color: rgba(245, 158, 11, 0.3) !important; }

                                .brand-text {
                                    background: linear-gradient(to right, var(--secondary), var(--primary));
                                    -webkit-background-clip: text;
                                    background-clip: text;
                                    -webkit-text-fill-color: transparent;
                                    text-shadow: 0 0 10px var(--glow-cyan);
                                }

                                #jettra-3d-bg {
                                    position: fixed;
                                    top: 0;
                                    left: 0;
                                    width: 100%;
                                    height: 100%;
                                    z-index: -1;
                                    pointer-events: none;
                                }
                            `;
                            document.head.appendChild(style);

                            // Three.js Background Initialization
                            const trackedWindows = [];
                            window.anchorTo3D = (elId, x, y, z) => {
                                const el = document.getElementById(elId);
                                if (el) {
                                    el.style.position = 'fixed';
                                    trackedWindows.push({ el, pos: new THREE.Vector3(x, y, z) });
                                }
                            };

                            const init3D = () => {
                                if (typeof THREE === 'undefined') return;
                                const scene = new THREE.Scene();
                                const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
                                const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
                                renderer.setSize(window.innerWidth, window.innerHeight);
                                renderer.domElement.id = 'jettra-3d-bg';
                                document.body.appendChild(renderer.domElement);

                                const grid = new THREE.GridHelper(50, 50, 0x007bff, 0x111111);
                                scene.add(grid);

                                const count = 200;
                                const positions = new Float32Array(count * 3);
                                const vels = [];
                                for(let i=0; i<count; i++) {
                                    const a = Math.random()*Math.PI*2;
                                    const r = 5 + Math.random()*15;
                                    positions[i*3] = Math.cos(a)*r;
                                    positions[i*3+1] = (Math.random()-0.5)*10;
                                    positions[i*3+2] = Math.sin(a)*r;
                                    vels.push({a, s: 0.002+Math.random()*0.005, r});
                                }
                                const geo = new THREE.BufferGeometry();
                                geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
                                const pts = new THREE.Points(geo, new THREE.PointsMaterial({color: 0x00ffff, size: 0.1, transparent: true, opacity: 0.6}));
                                scene.add(pts);

                                camera.position.set(0, 10, 30);
                                camera.lookAt(0, 0, 0);

                                const anim = () => {
                                    requestAnimationFrame(anim);
                                    const arr = pts.geometry.attributes.position.array;
                                    for(let i=0; i<count; i++) {
                                        vels[i].a += vels[i].s;
                                        arr[i*3] = Math.cos(vels[i].a)*vels[i].r;
                                        arr[i*3+2] = Math.sin(vels[i].a)*vels[i].r;
                                    }
                                    pts.geometry.attributes.position.needsUpdate = true;

                                    // Update Tracked Windows
                                    trackedWindows.forEach(win => {
                                        const v = win.pos.clone();
                                        v.project(camera);
                                        const x = (v.x * 0.5 + 0.5) * window.innerWidth;
                                        const y = (v.y * -0.5 + 0.5) * window.innerHeight;
                                        win.el.style.left = (x - win.el.offsetWidth / 2) + 'px';
                                        win.el.style.top = (y - win.el.offsetHeight / 2) + 'px';

                                        // Fade out if outside view or behind camera
                                        if (Math.abs(v.x) > 1.2 || Math.abs(v.y) > 1.2 || v.z > 1) {
                                            win.el.style.opacity = '0';
                                            win.el.style.pointerEvents = 'none';
                                        } else {
                                            win.el.style.opacity = '1';
                                            win.el.style.pointerEvents = 'auto';
                                        }
                                    });

                                    renderer.render(scene, camera);
                                };
                                anim();
                                window.addEventListener('resize', () => {
                                    camera.aspect = window.innerWidth / window.innerHeight;
                                    camera.updateProjectionMatrix();
                                    renderer.setSize(window.innerWidth, window.innerHeight);
                                });
                            };

                            const script = document.createElement('script');
                            script.src = 'https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js';
                            script.onload = init3D;
                            document.head.appendChild(script);
                        """);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void addScript(String src) {
        scripts.add(src);
    }

    public void addScriptContent(String content) {
        scriptContents.add(content);
    }

    public void addCss(String href) {
        cssLinks.add(href);
    }

    public String render() {
        StringBuilder head = new StringBuilder();
        for (String css : cssLinks) {
            head.append(String.format("<link href=\"%s\" rel=\"stylesheet\" />\n", css));
        }
        for (String script : scripts) {
            head.append(String.format("<script src=\"%s\"></script>\n", script));
        }
        for (String content : scriptContents) {
            head.append(String.format("<script>%s</script>\n", content));
        }

        // Flowbite JS needs to be at end of body usually, but we can put it here or
        // keep it separate in the logic
        // For simplicity, let's keep the core structure requested by user

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    %s
                </head>
                <body class="bg-gray-50 dark:bg-gray-900">
                    %s
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.2.1/flowbite.min.js"></script>
                </body>
                </html>
                """.formatted(title, head.toString(), content);
    }
}
