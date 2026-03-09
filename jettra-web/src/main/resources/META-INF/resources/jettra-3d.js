// Three.js Globals
let scene, camera, renderer, particles;
let particleVelocities = [];
const particlesCount = 200;

function initJettra3D() {
    scene = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    renderer.domElement.style.position = 'fixed';
    renderer.domElement.style.top = '0';
    renderer.domElement.style.left = '0';
    renderer.domElement.style.zIndex = '0'; // Behind everything
    renderer.domElement.id = 'jettra-3d-bg';
    document.body.appendChild(renderer.domElement);

    const ambientLight = new THREE.AmbientLight(0xffffff, 0.4);
    scene.add(ambientLight);

    const sunLight = new THREE.DirectionalLight(0xffffff, 1.0);
    scene.add(sunLight);

    const gridHelper = new THREE.GridHelper(40, 40, 0x007bff, 0x111111);
    scene.add(gridHelper);

    // Particles
    const positions = new Float32Array(particlesCount * 3);
    for (let i = 0; i < particlesCount; i++) {
        const angle = Math.random() * Math.PI * 2;
        const radius = 2 + Math.random() * 8;
        positions[i * 3] = Math.cos(angle) * radius;
        positions[i * 3 + 1] = (Math.random() - 0.5) * 10;
        positions[i * 3 + 2] = Math.sin(angle) * radius;
        particleVelocities.push({ angle: angle, speed: 0.005 + Math.random() * 0.01, radius: radius });
    }
    const particlesGeo = new THREE.BufferGeometry();
    particlesGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    particles = new THREE.Points(particlesGeo, new THREE.PointsMaterial({ color: 0x00ffff, size: 0.08, transparent: true, opacity: 0.6 }));
    scene.add(particles);

    camera.position.set(0, 5, 15);
    camera.lookAt(0, 0, 0);

    window.addEventListener('resize', () => {
        camera.aspect = window.innerWidth / window.innerHeight;
        camera.updateProjectionMatrix();
        renderer.setSize(window.innerWidth, window.innerHeight);
    });

    animate();
}

// Spatial Tracking
let trackedWindows = [];
window.anchorTo3D = (elId, x, y, z, tag = 'default') => {
    const el = document.getElementById(elId);
    if (el) {
        el.style.position = 'fixed';
        trackedWindows.push({ el, pos: new THREE.Vector3(x, y, z), tag });
    }
};

window.clearSpatialWindows = (tag) => {
    trackedWindows = trackedWindows.filter(win => {
        if (win.tag === tag) {
            win.el.remove();
            return false;
        }
        return true;
    });
};

function animate() {
    requestAnimationFrame(animate);
    const t = Date.now() * 0.001;

    if (particles) {
        const posArr = particles.geometry.attributes.position.array;
        for (let i = 0; i < particlesCount; i++) {
            const p = particleVelocities[i];
            p.angle += p.speed;
            posArr[i * 3] = Math.cos(p.angle) * p.radius;
            posArr[i * 3 + 2] = Math.sin(p.angle) * p.radius;
        }
        particles.geometry.attributes.position.needsUpdate = true;
    }

    // Update Tracked Windows
    trackedWindows.forEach(win => {
        const v = win.pos.clone();
        v.project(camera);
        const x = (v.x * 0.5 + 0.5) * window.innerWidth;
        const y = (v.y * -0.5 + 0.5) * window.innerHeight;
        win.el.style.left = (x - win.el.offsetWidth / 2) + 'px';
        win.el.style.top = (y - win.el.offsetHeight / 2) + 'px';

        // Visibility & Fading
        if (Math.abs(v.x) > 1.1 || Math.abs(v.y) > 1.1 || v.z > 1) {
            win.el.style.opacity = '0';
            win.el.style.pointerEvents = 'none';
        } else {
            win.el.style.opacity = '1';
            win.el.style.pointerEvents = 'auto';
        }
    });

    // Low hover effect for camera
    if (camera) {
        camera.position.x = Math.sin(t * 0.2) * 2;
        camera.lookAt(0, 0, 0);
    }

    renderer.render(scene, camera);
}

// Window Dragging Logic (Ported from JettraAgent)
let draggingWindow = null;
let dragOffset = { x: 0, y: 0 };
let maxZIndex = 2000;

function startWindowDrag(e, id) {
    const win = document.getElementById(id);
    if (!win) return;

    // If it was tracked, remove it from tracking to allow free drag
    trackedWindows = trackedWindows.filter(w => w.el !== win);

    draggingWindow = win;
    win.style.zIndex = ++maxZIndex;
    const rect = win.getBoundingClientRect();
    dragOffset.x = e.clientX - rect.left;
    dragOffset.y = e.clientY - rect.top;
    e.preventDefault();
}

document.addEventListener('mousemove', (e) => {
    if (draggingWindow) {
        draggingWindow.style.left = (e.clientX - dragOffset.x) + 'px';
        draggingWindow.style.top = (e.clientY - dragOffset.y) + 'px';
        draggingWindow.style.right = 'auto';
        draggingWindow.style.bottom = 'auto';
        draggingWindow.style.transform = 'none';
        draggingWindow.style.position = 'fixed';
    }
});

document.addEventListener('mouseup', () => {
    draggingWindow = null;
});

// Initialize on load
window.addEventListener('load', () => {
    initJettra3D();
});
