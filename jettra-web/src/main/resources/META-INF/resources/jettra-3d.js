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

    // Cartesian Plane (Axes & Grid)
    const axesHelper = new THREE.AxesHelper(20);
    scene.add(axesHelper);

    const gridHelper = new THREE.GridHelper(40, 40, 0x007bff, 0x111111);
    scene.add(gridHelper);

    // X-Z Grid (as a base plane)
    const xzGrid = new THREE.GridHelper(40, 40, 0x333333, 0x111111);
    xzGrid.rotation.x = Math.PI / 2;
    scene.add(xzGrid);

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

// 3D Spatial Dragging Logic
let dragging3DWindow = null;
let is3DDragMoved = false; // To differentiate between a click and a drag
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
// Plane at the depth of the object being dragged
let dragPlane = new THREE.Plane(new THREE.Vector3(0, 0, 1), 0);
let dragOffset3D = new THREE.Vector3();

window.start3DDrag = function (e, id) {
    const win = trackedWindows.find(w => w.el.id === id);
    if (!win) return;

    dragging3DWindow = win;
    is3DDragMoved = false;
    win.el.style.zIndex = ++maxZIndex;

    // Determine the plane where the object is located
    dragPlane.setFromNormalAndCoplanarPoint(new THREE.Vector3(0, 0, 1), win.pos);

    // Calculate mouse position in normalized device coordinates
    mouse.x = (e.clientX / window.innerWidth) * 2 - 1;
    mouse.y = -(e.clientY / window.innerHeight) * 2 + 1;

    // Raycast to find intersection point
    raycaster.setFromCamera(mouse, camera);
    const intersect = new THREE.Vector3();
    raycaster.ray.intersectPlane(dragPlane, intersect);

    // Calculate offset between object center and mouse intersection
    dragOffset3D.copy(intersect).sub(win.pos);

    e.preventDefault();
    e.stopPropagation(); // Avoid triggering document clicks
};

window.is3DWindowMoved = function () {
    return is3DDragMoved;
};

document.addEventListener('mousemove', (e) => {
    if (draggingWindow) {
        draggingWindow.style.left = (e.clientX - dragOffset.x) + 'px';
        draggingWindow.style.top = (e.clientY - dragOffset.y) + 'px';
        draggingWindow.style.right = 'auto';
        draggingWindow.style.bottom = 'auto';
        draggingWindow.style.transform = 'none';
        draggingWindow.style.position = 'fixed';
    }

    if (dragging3DWindow) {
        is3DDragMoved = true;

        // Unproject mouse coordinates into the 3D plane
        mouse.x = (e.clientX / window.innerWidth) * 2 - 1;
        mouse.y = -(e.clientY / window.innerHeight) * 2 + 1;

        raycaster.setFromCamera(mouse, camera);
        const intersect = new THREE.Vector3();
        raycaster.ray.intersectPlane(dragPlane, intersect);

        // Apply offset to move the object smoothly
        dragging3DWindow.pos.copy(intersect).sub(dragOffset3D);
        dragging3DWindow.el.style.cursor = 'grabbing';
    }
});

document.addEventListener('mouseup', () => {
    draggingWindow = null;
    if (dragging3DWindow) {
        dragging3DWindow.el.style.cursor = 'pointer';

        if (is3DDragMoved && dragging3DWindow.el.id.startsWith('col-3d-node-')) {
            // ID format: col-3d-node-{dbName}-{colName}
            const parts = dragging3DWindow.el.id.split('-');
            const dbName = parts[3];
            const colName = parts.slice(4).join('-'); // Re-join if colName has hyphens
            
            const key = `jettra_col_pos_${dbName}_${colName}`;
            const posToSave = {
                x: dragging3DWindow.pos.x,
                y: dragging3DWindow.pos.y,
                z: dragging3DWindow.pos.z
            };
            localStorage.setItem(key, JSON.stringify(posToSave));
            console.log(`[3D] Saved position for ${dbName}/${colName}:`, posToSave);
        }

        // Reset state after a short delay so click events don't trigger immediately
        setTimeout(() => {
            is3DDragMoved = false;
        }, 50);
        dragging3DWindow = null;
    }
});

// Initialize on load
window.addEventListener('load', () => {
    initJettra3D();
});
