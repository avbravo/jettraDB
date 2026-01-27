

        const username = localStorage.getItem('jettra_username') || 'Unknown';
        const userProfile = localStorage.getItem('jettra_profile') || 'end-user';
        document.getElementById('logged-username').textContent = username;

        const isAdmin = userProfile === 'super-user';
        const isSuperUser = userProfile === 'super-user';
        const isEndUser = userProfile === 'end-user';

        // Hide New Database button for end-users and management
        const btnNewDb = document.querySelector('.btn-plus');
        if (btnNewDb) {
            btnNewDb.style.display = (userProfile === 'super-user') ? 'flex' : 'none';
        }
        let currentView = 'cluster';
        let allNodes = [];
        let allDatabases = [];
        let allUsers = [];
        let allSequences = [];

        // Users Pagination
        let userPage = 1;
        const usersPerPage = 5;
        let selectedNodeId = null;
        let dbToDelete = null;

        // Apply initial role-based UI restrictions
        const navSecurity = document.getElementById('nav-security');
        if (navSecurity) {
            navSecurity.style.display = (userProfile === 'super-user' || userProfile === 'management') ? 'block' : 'none';
        }

        const SUPPORTED_ENGINES = [
            { name: 'Document', icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z' },
            { name: 'Column', icon: 'M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z' },
            { name: 'Graph', icon: 'M7 20l4-16m2 16l4-16M6 9h14M4 15h14' },
            { name: 'Vector', icon: 'M13 10V3L4 14h7v7l9-11h-7z' },
            { name: 'Object', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4' },
            { name: 'Key-value', icon: 'M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z' },
            { name: 'Geospatial', icon: 'M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
            { name: 'Time-Series', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
            { name: 'Files', icon: 'M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z' }
        ];

        function showAlert(title, message, type = 'error') {
            const modal = document.getElementById('alert-modal');
            const titleEl = document.getElementById('alert-title');
            const msgEl = document.getElementById('alert-message');
            const iconContainer = document.getElementById('alert-icon-container');
            const card = modal.querySelector('.card');

            titleEl.textContent = title;
            msgEl.textContent = message;

            if (type === 'error') {
                card.style.borderTopColor = '#ef4444';
                iconContainer.style.background = 'rgba(239, 68, 68, 0.1)';
                iconContainer.innerHTML = '<svg style="width: 24px; height: 24px; color: #ef4444;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>';
            } else {
                card.style.borderTopColor = '#10b981';
                iconContainer.style.background = 'rgba(16, 185, 129, 0.1)';
                iconContainer.innerHTML = '<svg style="width: 24px; height: 24px; color: #10b981;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>';
            }

            modal.classList.remove('hidden');
        }

        function closeAlertModal() {
            document.getElementById('alert-modal').classList.add('hidden');
        }

        const nodeGrid = document.getElementById('node-grid');
        const groupGrid = document.getElementById('group-grid');

        function toggleSidebar() {
            const sidebar = document.getElementById('sidebar');
            const mainContent = document.querySelector('.main-content');

            if (window.innerWidth <= 768) {
                sidebar.classList.toggle('show-mobile');
            } else {
                sidebar.classList.toggle('collapsed');
                if (sidebar.classList.contains('collapsed')) {
                    mainContent.style.marginLeft = '0';
                    mainContent.style.width = '100%';
                } else {
                    mainContent.style.marginLeft = 'var(--sidebar-width)';
                    mainContent.style.width = 'calc(100% - var(--sidebar-width))';
                }
            }
        }

        function showUserInfo() {
            document.getElementById('user-info-username').textContent = username;
            document.getElementById('user-info-profile').textContent = userProfile;
            document.getElementById('user-info-initial').textContent = username.charAt(0).toUpperCase();
            document.getElementById('user-info-modal').classList.remove('hidden');
        }

        function closeUserInfoModal() {
            document.getElementById('user-info-modal').classList.add('hidden');
        }

        function showView(viewId) {
            currentView = viewId;
            document.getElementById('view-cluster').classList.toggle('hidden', viewId !== 'cluster');
            document.getElementById('view-security').classList.toggle('hidden', viewId !== 'security');
            document.getElementById('view-query').classList.toggle('hidden', viewId !== 'query');
            document.getElementById('view-sequences').classList.toggle('hidden', viewId !== 'sequences');
            document.getElementById('view-password').classList.toggle('hidden', viewId !== 'password');
            document.getElementById('view-document-explorer').classList.toggle('hidden', viewId !== 'document-explorer');

            document.getElementById('nav-cluster').classList.toggle('active', viewId === 'cluster');
            document.getElementById('nav-security').classList.toggle('active', viewId === 'security');
            document.getElementById('nav-query').classList.toggle('active', viewId === 'query');
            document.getElementById('nav-password').classList.toggle('active', viewId === 'password');

            if (viewId === 'sequences') {
                loadSequences();
            }

            // Close sidebar on mobile
            if (window.innerWidth <= 768) {
                document.getElementById('sidebar').classList.remove('show');
            }

            refreshData();
        }

        function refreshData() {
            // Always fetch databases to keep the tree explorer updated
            fetchDatabases();

            if (currentView === 'security') {
                fetchUsers();
                fetchRoles();
                if (document.getElementById('user-db-roles-body').innerHTML.includes('Loading')) {
                    renderUserDbRoles();
                }
            }
        }

        setInterval(refreshData, 5000);
        refreshData();

        async function fetchNodes() {
            try {
                const response = await fetchWithAuth('/api/monitor/nodes');
                if (response.ok) {
                    const data = await safeJson(response);
                    console.log('Nodes fetched:', data);
                    allNodes = data || [];
                }
                if (selectedNodeId) updateModalContent();
            } catch (error) {
                console.error('Error fetching nodes:', error);
            }
        }

        // Refresh nodes every 5 seconds if on cluster view
        setInterval(() => {
            if (currentView === 'cluster') {
                fetchNodes();
            }
        }, 2000);


        async function fetchDatabases() {
            try {
                const response = await fetchWithAuth('/api/db');
                if (response.status === 401) return logout();
                if (!response.ok) throw new Error('HTTP ' + response.status);
                allDatabases = await safeJson(response) || [];

                // Fetch users and roles to have info for the tree
                await fetchUsers();
                await fetchRoles();
                await fetchSequences();

                // Fetch collections for all databases to populate tree
                for (let db of allDatabases) {
                    try {
                        const colRes = await fetchWithAuth(`/api/db/${db.name}/collections`);
                        if (colRes.ok) db.collections = await safeJson(colRes);
                    } catch (e) { console.error('Error fetching collections for ' + db.name, e); }
                }

                renderDbTree(allDatabases);
            } catch (error) {
                console.error('Error fetching databases:', error);
            }
        }

        function getUserPrivileges(dbName) {
            if (userProfile === 'super-user') {
                return { admin: true, read: true, write: true };
            }
            if (!dbName) return { admin: false, read: false, write: false };

            const currentUser = allUsers.find(u => u.username === username);
            if (!currentUser || !currentUser.roles) return { admin: false, read: false, write: false };

            // Find roles that apply to this database (case-insensitive) or to all databases
            const dbRoles = allSystemRoles.filter(role => {
                if (!currentUser.roles.includes(role.name)) return false;
                return role.database === '_all' || (role.database && role.database.toLowerCase() === dbName.toLowerCase());
            });

            const privileges = {
                admin: dbRoles.some(r => r.privileges?.includes('ADMIN') || r.name?.toLowerCase().startsWith('admin_') || r.name?.toLowerCase() === 'admin'),
                read: dbRoles.some(r => r.privileges?.includes('READ') || r.privileges?.includes('ADMIN') || r.name?.toLowerCase().startsWith('admin_') || r.name?.toLowerCase().startsWith('reader_')),
                write: dbRoles.some(r => r.privileges?.includes('WRITE') || r.privileges?.includes('ADMIN') || r.name?.toLowerCase().startsWith('admin_') || r.name?.toLowerCase().startsWith('writer_'))
            };
            return privileges;
        }

        function renderCollectionItems(dbName, collections, engine) {
            if (!collections || collections.length === 0) return '<div style="color: #475569; font-size: 0.75rem; padding-left: 1rem;">Sin colecciones</div>';
            const privs = getUserPrivileges(dbName);

            return collections.map(col => `
                <div class="tree-type-item" style="justify-content: space-between; padding-right: 0.5rem;" onclick="toggleSubtree('sub-${dbName}-${col}'); ${engine === 'Document' ? `selectCollection('${dbName}', '${col}', '${engine}')` : ''}">
                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                        <svg style="width: 10px; height: 10px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2H6a2 2 0 00-2 2z"></path></svg>
                        <span style="cursor: pointer; font-weight: 600;">${col}</span>
                    </div>
                    <div style="display: flex; gap: 4px;">
                        ${privs.write || privs.admin ? `
                        <div class="action-icon" style="width:16px; height:16px;" title="Añadir Documento" onclick="event.stopPropagation(); addDocumentFromTree('${dbName}', '${col}', '${engine}')">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                        </div>
                        <div class="action-icon" style="width:16px; height:16px;" title="Renombrar" onclick="event.stopPropagation(); openCollectionModal('${dbName}', '${col}', '${engine}')">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                        </div>
                        ` : ''}
                        ${privs.admin ? `
                        <div class="action-icon" style="width:16px; height:16px;" title="Eliminar" onclick="event.stopPropagation(); handleDeleteCol('${dbName}', '${col}')">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </div>
                        ` : ''}
                    </div>
                </div>
                <div id="sub-${dbName}-${col}" class="${expandedNodes.has(`sub-${dbName}-${col}`) ? 'show' : 'hidden'}" style="padding-left: 1.5rem; border-left: 1px solid rgba(255,255,255,0.05); margin-left: 0.5rem;">
                    <div class="tree-type-item" onclick="selectCollection('${dbName}', '${col}', '${engine}')" style="font-size: 0.75rem; color: var(--primary);">
                        <svg style="width: 12px; height: 12px; margin-right: 0.5rem;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
                         Documentos (CRUD)
                    </div>
                    <div class="tree-type-item" onclick="showAlert('Info', 'Gestión de índices para ${col}', 'info')" style="font-size: 0.75rem; color: #94a3b8;">
                        <svg style="width: 12px; height: 12px; margin-right: 0.5rem;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
                         index
                    </div>
                    <div class="tree-type-item" onclick="openSequenceModalFromTree('${dbName}', '${col}')" style="font-size: 0.75rem; color: #94a3b8;">
                        <svg style="width: 12px; height: 12px; margin-right: 0.5rem;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                         secuency
                    </div>
                    <div class="tree-type-item" onclick="showAlert('Info', 'Definición de reglas para ${col}', 'info')" style="font-size: 0.75rem; color: #94a3b8;">
                        <svg style="width: 12px; height: 12px; margin-right: 0.5rem;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
                         rules
                    </div>
                </div>
            `).join('');
        }

        function openCollectionModal(dbName, colName = '', engine = 'Document') {
            document.getElementById('col-db-name').value = dbName;
            document.getElementById('col-old-name').value = colName;
            document.getElementById('col-name-input').value = colName;
            const engineSelect = document.getElementById('col-engine-select');
            engineSelect.value = engine;

            // Lock engine selection if context is provided
            if (engine) {
                engineSelect.disabled = true;
                engineSelect.style.opacity = '0.7';
                engineSelect.style.cursor = 'not-allowed';
            } else {
                engineSelect.disabled = false;
                engineSelect.style.opacity = '1';
                engineSelect.style.cursor = 'pointer';
            }

            document.getElementById('col-modal-title').textContent = colName ? 'Editar Colección' : 'Nueva Colección';
            document.getElementById('col-submit-btn').textContent = colName ? 'Guardar Cambios' : 'Crear Colección';
            document.getElementById('collection-modal').classList.remove('hidden');
        }

        let currentSelectedCollection = null;
        let currentSelectedDatabase = null;
        let expandedNodes = new Set();

        async function safeJson(response) {
            const text = await response.text();
            if (!text) return null;
            try {
                return JSON.parse(text);
            } catch (e) {
                console.error('Failed to parse JSON:', text);
                return null;
            }
        }

        async function executeSqlQuery() {
            const sql = document.getElementById('sql-input').value.trim();
            if (!sql) {
                showAlert('Warning', 'Please enter an SQL statement.');
                return;
            }

            const resultContainer = document.getElementById('query-result-container');
            const resultContent = document.getElementById('query-result-content');
            const statusBadge = document.getElementById('query-status-badge');

            resultContainer.classList.remove('hidden');
            resultContent.textContent = 'Executing query...';
            statusBadge.textContent = 'PENDING';
            statusBadge.style.background = 'rgba(245, 158, 11, 0.1)';
            statusBadge.style.color = '#f59e0b';

            try {
                const resolveRefs = document.getElementById('sql-resolve-refs').checked;
                const response = await fetchWithAuth('/api/v1/sql', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ sql, resolveRefs })
                });

                const data = await safeJson(response);

                resultContent.textContent = JSON.stringify(data, null, 2);

                if (response.ok) {
                    statusBadge.textContent = 'SUCCESS';
                    statusBadge.style.background = 'rgba(16, 185, 129, 0.1)';
                    statusBadge.style.color = '#10b981';
                } else {
                    statusBadge.textContent = 'ERROR ' + response.status;
                    statusBadge.style.background = 'rgba(239, 68, 68, 0.1)';
                    statusBadge.style.color = '#ef4444';
                }
            } catch (error) {
                resultContent.textContent = 'Error: ' + error.message;
                statusBadge.textContent = 'FAILED';
                statusBadge.style.background = 'rgba(239, 68, 68, 0.1)';
                statusBadge.style.color = '#ef4444';
            }
        }


        async function fetchSequences() {
            try {
                const response = await fetchWithAuth('/api/v1/sequence');
                if (response.ok) {
                    allSequences = await safeJson(response) || [];
                }
            } catch (error) {
                console.error('Error fetching sequences:', error);
            }
        }

        function renderSequenceItems(dbName, col) {
            const sequences = allSequences.filter(s => s.database === dbName && (s.name.startsWith(col) || s.name.includes('.' + col + '.')));
            if (sequences.length === 0) return '<div style="color: #475569; font-size: 0.7rem; padding-left: 0.5rem; padding-bottom: 0.5rem;">No sequences</div>';

            return sequences.map(s => `
                <div class="tree-type-item" onclick="showView('sequences')" style="font-size: 0.75rem; color: #e2e8f0; justify-content: space-between; padding-right: 0.5rem;">
                    <div style="display: flex; align-items: center;">
                        <svg style="width: 10px; height: 10px; margin-right: 0.4rem;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                        ${s.name}
                    </div>
                    <span style="color: #10b981; font-family: monospace;">${s.currentValue}</span>
                </div>
            `).join('');
        }

        function renderDbSequenceItems(dbName) {
            const sequences = allSequences.filter(s => s.database === dbName);
            if (sequences.length === 0) return '<div style="color: #475569; font-size: 0.75rem; padding-left: 1rem; padding-bottom: 0.5rem;">No sequences</div>';

            return sequences.map(s => `
                <div class="tree-type-item" onclick="showView('sequences')" style="font-size: 0.75rem; color: #e2e8f0; justify-content: space-between; padding-right: 0.5rem; padding-left: 1rem;">
                    <div style="display: flex; align-items: center;">
                        <svg style="width: 10px; height: 10px; margin-right: 0.4rem;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                        ${s.name}
                    </div>
                    <span style="color: #10b981; font-family: monospace;">${s.currentValue}</span>
                </div>
            `).join('');
        }

        async function loadSequences() {
            const list = document.getElementById('sequences-list');
            list.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #64748b;">Loading sequences...</td></tr>';

            await fetchSequences();
            const sequences = allSequences;

            if (sequences.length === 0) {
                list.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #64748b;">No sequences found.</td></tr>';
                return;
            }

            list.innerHTML = sequences.map(s => `
                <tr>
                    <td style="font-weight: 600; color: white;">${s.name}</td>
                    <td><span style="font-size: 0.75rem; background: rgba(99, 102, 241, 0.1); color: var(--primary); padding: 0.2rem 0.5rem; border-radius: 4px;">${s.database}</span></td>
                    <td style="font-family: 'Fira Code', monospace; color: #10b981;">${s.currentValue}</td>
                    <td style="color: #94a3b8;">${s.increment}</td>
                    <td>
                        <div style="display: flex; gap: 0.5rem;">
                            <button class="view-mode-btn" onclick="nextSequence('${s.name}')">NEXT</button>
                            <button class="view-mode-btn" style="color: #ef4444;" onclick="deleteSequence('${s.name}')">DELETE</button>
                        </div>
                    </td>
                </tr>
            `).join('');
        }

        function showCreateSequenceModal() {
            document.getElementById('sequence-modal').classList.remove('hidden');
        }

        function closeSequenceModal() {
            document.getElementById('sequence-modal').classList.add('hidden');
        }

        async function createSequence() {
            const name = document.getElementById('seq-name').value;
            const db = document.getElementById('seq-db').value;
            const start = parseInt(document.getElementById('seq-start').value);
            const inc = parseInt(document.getElementById('seq-inc').value);

            if (!name || !db) {
                showAlert('Error', 'Name and Database are required.');
                return;
            }

            try {
                const response = await fetchWithAuth('/api/v1/sequence', {
                    method: 'POST',
                    body: JSON.stringify({ name, database: db, startValue: start, increment: inc })
                });

                if (response.ok) {
                    closeSequenceModal();
                    loadSequences();
                } else {
                    showAlert('Error', 'Failed to create sequence: ' + response.status);
                }
            } catch (error) {
                showAlert('Error', error.message);
            }
        }

        function openSequenceModalFromTree(dbName, colName) {
            document.getElementById('seq-name').value = colName + '_seq';
            document.getElementById('seq-db').value = dbName;
            document.getElementById('seq-start').value = '1';
            document.getElementById('seq-inc').value = '1';
            showCreateSequenceModal();
        }

        async function nextSequence(name) {
            try {
                const response = await fetchWithAuth(`/api/v1/sequence/${name}/next`);
                if (response.ok) {
                    loadSequences();
                } else {
                    showAlert('Error', 'Failed to increment sequence: ' + response.status);
                }
            } catch (error) {
                showAlert('Error', error.message);
            }
        }

        async function deleteSequence(name) {
            if (!confirm(`Are you sure you want to delete sequence '${name}'?`)) return;
            try {
                const response = await fetchWithAuth(`/api/v1/sequence/${name}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    loadSequences();
                } else {
                    showAlert('Error', 'Failed to delete sequence: ' + response.status);
                }
            } catch (error) {
                showAlert('Error', error.message);
            }
        }
        async function fetchWithAuth(url, options = {}) {
            const token = localStorage.getItem('jettra_token');
            const headers = {
                'Authorization': `Bearer ${token}`,
                ...options.headers
            };
            try {
                const response = await fetch(url, { ...options, headers });
                if (response.status === 401 || response.status === 403) {
                    window.location.href = '/login.html';
                    throw new Error('Unauthorized');
                }
                return response;
            } catch (error) {
                if (error instanceof TypeError) {
                    console.error("Network error:", error);
                    throw new Error("Network error: Failed to fetch. Please check your connection to the server.");
                }
                throw error;
            }
        }

        function selectCollection(dbName, colName, engine) {
            currentSelectedDatabase = dbName;
            currentSelectedCollection = colName;
            currentPage = 1; // Reset to first page on collection change

            showView('document-explorer');
            document.getElementById('doc-explorer-title').textContent = `${colName} (${engine})`;
            document.getElementById('doc-explorer-subtitle').textContent = `Exploring collection in database: ${dbName}`;

            loadDocuments(colName);
        }

        function addDocumentFromTree(dbName, colName, engine) {
            selectCollection(dbName, colName, engine);
            setTimeout(() => {
                openAddDocModal();
            }, 100);
        }

        let currentDocViewMode = 'table';
        let loadedDocuments = [];
        let currentPage = 1;
        const pageSize = 20;

        function nextPage() {
            currentPage++;
            loadDocuments(currentSelectedCollection);
        }

        function prevPage() {
            if (currentPage > 1) {
                currentPage--;
                loadDocuments(currentSelectedCollection);
            }
        }

        function updatePaginationControls() {
            document.getElementById('page-indicator').textContent = `Page ${currentPage}`;
            document.getElementById('btn-prev-page').disabled = currentPage <= 1;
            // Next button is disabled if we fetched less than pageSize, meaning end of list
            document.getElementById('btn-next-page').disabled = loadedDocuments.length < pageSize;
        }

        function setDocViewMode(mode) {
            currentDocViewMode = mode;
            document.querySelectorAll('.view-mode-btn').forEach(btn => btn.classList.remove('active'));
            document.getElementById(`view-mode-${mode}`).classList.add('active');
            renderCurrentDocuments();
        }

        async function loadDocuments(colName) {
            const container = document.getElementById('document-list-container');
            container.innerHTML = '<div style="padding: 2rem; text-align: center; color: #94a3b8;">Fetching records...</div>';

            // Get search term
            const searchInput = document.getElementById('doc-search-input');
            const searchTerm = searchInput ? searchInput.value.trim() : '';

            try {
                const nodesResp = await fetchWithAuth('/api/monitor/nodes');
                const nodes = await safeJson(nodesResp) || [];
                const storeNode = nodes.find(n => n.role === 'STORAGE' && n.status === 'ONLINE');

                if (!storeNode) {
                    container.innerHTML = '<div style="padding: 2rem; text-align: center; color: #ef4444;">No online STORAGE nodes found.</div>';
                    return;
                }

                // Append pagination and search parameters
                const resolveRefs = document.getElementById('doc-resolve-refs').checked;
                let targetUrl = `http://${storeNode.address}/api/v1/document/${colName}?page=${currentPage}&size=${pageSize}`;
                if (searchTerm) {
                    targetUrl += `&search=${encodeURIComponent(searchTerm)}`;
                }
                if (resolveRefs) {
                    targetUrl += `&resolveRefs=true`;
                }

                const resp = await fetchWithAuth('/api/db/proxy/document', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ url: targetUrl, method: 'GET' })
                });

                if (resp.ok) {
                    const data = await safeJson(resp);
                    loadedDocuments = data ? data.map(d => JSON.parse(d)) : [];
                    renderCurrentDocuments();
                    updatePaginationControls();
                } else {
                    container.innerHTML = `<div style="padding: 2rem; text-align: center; color: #ef4444;">Collection is empty or unreachable (Status: ${resp.status}).</div>`;
                    loadedDocuments = [];
                    updatePaginationControls();
                }
            } catch (e) {
                container.innerHTML = '<div style="padding: 2rem; text-align: center; color: #ef4444;">Error: ' + e.message + '</div>';
            }
        }

        function searchDocuments() {
            currentPage = 1;
            loadDocuments(currentSelectedCollection);
        }

        function renderCurrentDocuments() {
            const container = document.getElementById('document-list-container');
            if (loadedDocuments.length === 0) {
                container.innerHTML = `
                    <div style="padding: 3rem; text-align: center; color: #475569;">
                        <svg style="width: 48px; height: 48px; margin: 0 auto 1rem; opacity: 0.3;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0a2 2 0 01-2 2H6a2 2 0 01-2-2m16 0l-2.586 2.586a2 2 0 01-2.828 0L12 14l-2.586 2.586a2 2 0 01-2.828 0L4 13"></path></svg>
                        <p>No documents found in this collection.</p>
                        <button class="btn" style="margin-top: 1rem; font-size: 0.8rem;" onclick="addSampleDoc()">Insert Example</button>
                    </div>
                `;
                return;
            }

            if (currentDocViewMode === 'table') {
                renderDocumentsTable(container);
            } else if (currentDocViewMode === 'json') {
                renderDocumentsJSON(container);
            } else {
                renderDocumentsTree(container);
            }
        }

        function renderDocumentsTable(container) {
            const keys = new Set();
            loadedDocuments.forEach(doc => Object.keys(doc).forEach(k => keys.add(k)));
            const headerKeys = Array.from(keys).filter(k => k !== '_id' && k !== 'jettraID');

            let html = '<div style="overflow-x: auto;"><table class="doc-table"><thead><tr>';
            html += '<th>jettraID</th>';
            headerKeys.slice(0, 5).forEach(k => html += `<th>${k}</th>`);
            html += '<th style="text-align: right;">Acciones</th></tr></thead><tbody>';

            loadedDocuments.forEach(doc => {
                const jid = doc.jettraID || 'N/A';
                html += `<tr><td style="font-family: monospace; font-size: 0.7rem; color: var(--primary); font-weight: 600;">${jid}</td>`;
                headerKeys.slice(0, 5).forEach(k => {
                    let val = doc[k];
                    if (val === undefined) val = '<span style="opacity: 0.3">-</span>';
                    else if (typeof val === 'object') val = '<span style="color: #64748b">{...}</span>';
                    html += `<td>${val}</td>`;
                });
                html += `
                    <td style="text-align: right;">
                        <div style="display: flex; gap: 0.75rem; justify-content: flex-end;">
                            <div class="action-icon" style="width:18px; height:18px;" title="History/Versions" onclick="showDocVersions('${jid}')">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            </div>
                            <div class="action-icon" style="width:18px; height:18px;" title="Editar" onclick="editDocument('${jid}')">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                            </div>
                            <div class="action-icon" style="width:18px; height:18px;" title="Eliminar" onclick="deleteDocument('${jid}')">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                            </div>
                        </div>
                    </td>
                </tr>`;
            });
            html += '</tbody></table></div>';
            container.innerHTML = html;
        }

        function renderDocumentsJSON(container) {
            container.innerHTML = `<div class="json-viewer">
                <div style="margin-bottom: 1rem; display: flex; gap: 0.5rem; justify-content: flex-end;">
                    <span style="color: #64748b; font-size: 0.8rem; align-self: center;">Actions for visible documents:</span>
                    ${loadedDocuments.map(d => `
                        <button class="btn" style="padding: 0.2rem 0.5rem; font-size: 0.75rem;" onclick="showDocVersions('${d.jettraID}')" title="Versions: ${d.jettraID}">
                            🕒 ${d.jettraID}
                        </button>
                    `).join('')}
                </div>
                ${JSON.stringify(loadedDocuments, null, 2)}
            </div>`;
        }

        function renderDocumentsTree(container) {
            let html = '<div class="tree-viewer">';
            loadedDocuments.forEach((doc, idx) => {
                html += `<div style="margin-bottom: 1rem; padding-bottom: 1rem; border-bottom: 1px solid rgba(255,255,255,0.05); position: relative;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                         <div style="color: #64748b; font-size: 0.7rem;">Document [${idx}]</div>
                         <div class="action-icon" style="width:16px; height:16px; cursor: pointer;" title="History/Versions" onclick="showDocVersions('${doc.jettraID}')">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                        </div>
                        <div class="action-icon" style="width:16px; height:16px; cursor: pointer;" title="Editar" onclick="editDocument('${doc.jettraID}')">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </div>
                        <div class="action-icon" style="width:16px; height:16px; cursor: pointer;" title="Eliminar" onclick="deleteDocument('${doc.jettraID}')">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </div>
                    </div>
                    ${renderObjectAsTree(doc)}
                </div>`;
            });
            html += '</div>';
            container.innerHTML = html;
        }

        function renderObjectAsTree(obj) {
            let html = '';
            for (const key in obj) {
                const val = obj[key];
                const isArr = Array.isArray(val);
                html += `<div class="tree-node">`;
                html += `<span class="tree-key">${key}:</span> `;
                if (val !== null && typeof val === 'object') {
                    html += `<span>${isArr ? '[' : '{'}</span>${renderObjectAsTree(val)}<span>${isArr ? ']' : '}'}</span>`;
                } else {
                    const type = typeof val;
                    const cssClass = type === 'string' ? 'tree-val-string' : (type === 'number' ? 'tree-val-number' : 'tree-val-bool');
                    html += `<span class="${cssClass}">${type === 'string' ? '"' + val + '"' : val}</span>`;
                }
                html += `</div>`;
            }
            return html;
        }

        function toggleSubtree(id) {
            const el = document.getElementById(id);
            if (el) {
                el.classList.toggle('hidden');
                el.classList.toggle('show');
                if (el.classList.contains('show')) {
                    expandedNodes.add(id);
                } else {
                    expandedNodes.delete(id);
                }
            }
        }

        function editDocument(jid) {
            const doc = loadedDocuments.find(d => d.jettraID === jid);
            if (!doc) return;

            // Set current jettraID in a hidden field if we want to update same record
            // For now, we'll just put it in the modal.
            document.getElementById('doc-json').value = JSON.stringify(doc, (key, value) => {
                if (key === 'jettraID') return undefined; // Let engine handle ID if we want to generate new or keep it
                return value;
            }, 2);

            // We'll add a way to remember we are editing
            document.getElementById('doc-modal-title').textContent = 'Editar Documento';
            document.getElementById('doc-id-input').value = jid;
            showModal('doc-modal');
        }

        let docToDeleteId = null;

        function deleteDocument(jid) {
            verifyDeleteDoc(jid);
        }

        function verifyDeleteDoc(jid) {
            docToDeleteId = jid;
            document.getElementById('delete-doc-id-label').textContent = jid;
            document.getElementById('doc-delete-modal').classList.remove('hidden');
        }

        function closeDocDeleteModal() {
            docToDeleteId = null;
            document.getElementById('doc-delete-modal').classList.add('hidden');
        }

        async function confirmDeleteDoc() {
            if (!docToDeleteId) return;

            try {
                const nodesResp = await fetchWithAuth('/api/monitor/nodes');
                const nodes = await safeJson(nodesResp) || [];
                const storeNode = nodes.find(n => n.role === 'STORAGE' && n.status === 'ONLINE');

                const targetUrl = `http://${storeNode.address}/api/v1/document/${currentSelectedCollection}/${encodeURIComponent(docToDeleteId)}`;
                const resp = await fetchWithAuth('/api/db/proxy/document', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ url: targetUrl, method: 'DELETE' })
                });

                if (resp.ok) {
                    showAlert('Registro eliminado', `El documento ${docToDeleteId} ha sido eliminado.`, 'success');
                    closeDocDeleteModal();
                    loadDocuments(currentSelectedCollection);
                } else {
                    const errText = await resp.text();
                    showAlert('Error al eliminar', `No se pudo eliminar el documento. Estado: ${resp.status}. ${errText}`);
                }
            } catch (e) {
                showAlert('Error: ' + e.message);
            }
        }

        function openAddDocModal() {
            if (!currentSelectedCollection) {
                showAlert('Please select a collection first');
                return;
            }
            document.getElementById('doc-id-input').value = '';
            document.getElementById('doc-modal-title').textContent = 'Nuevo Documento';
            document.getElementById('doc-json').value = '{\n  "name": "Nuevo Documento"\n }';
            showModal('doc-modal');
        }

        async function saveDocument() {
            const json = document.getElementById('doc-json').value;
            const existingId = document.getElementById('doc-id-input').value;
            try {
                // Discover store node
                const nodesResp = await fetchWithAuth('/api/monitor/nodes');
                const nodes = await safeJson(nodesResp) || [];
                const storeNode = nodes.find(n => n.role === 'STORAGE' && n.status === 'ONLINE');

                if (!storeNode) {
                    showAlert('Error: No online STORAGE nodes found');
                    return;
                }

                let url = `http://${storeNode.address}/api/v1/document/${currentSelectedCollection}`;
                if (existingId) {
                    url += `?jettraID=${encodeURIComponent(existingId)}`;
                }

                let resp;
                try {
                    const docObj = JSON.parse(json);
                    // Restriction: User cannot edit system fields
                    if (docObj._version) delete docObj._version;
                    if (docObj._lastModified) delete docObj._lastModified;
                    // docObj._version = undefined; // Alternative

                    const payload = JSON.stringify(docObj);

                    resp = await fetchWithAuth('/api/db/proxy/document', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            url: url,
                            method: 'POST',
                            payload: payload
                        })
                    });
                } catch (e) {
                    console.error("JSON parse error", e);
                    showAlert('Error de Formato JSON', 'El JSON proporcionado no es válido: ' + e.message + '. Por favor corríjalo antes de guardar.', 'error');
                    return;
                }

                if (resp.ok) {
                    // Simpler success message as requested
                    showAlert('Registro Guardado', 'El documento se ha guardado correctamente.', 'success');
                    closeModal('doc-modal');
                    loadDocuments(currentSelectedCollection);
                } else {
                    showAlert('Error al guardar: ' + resp.status);
                }
            } catch (e) {
                showAlert('Error: ' + e.message);
            }
        }
        async function addSampleDoc() {
            document.getElementById('doc-json').value = JSON.stringify({
                name: "Alice Cooper",
                email: "alice@jettra.io",
                status: "active",
                tags: ["demo", "premium"]
            }, null, 2);
            document.getElementById('doc-modal').classList.remove('hidden');
        }

        function showModal(id) {
            document.getElementById(id).classList.remove('hidden');
        }

        function closeModal(id) {
            document.getElementById(id).classList.add('hidden');
        }

        function closeCollectionModal() {
            document.getElementById('collection-modal').classList.add('hidden');
        }

        async function handleSubmitCollection() {
            const dbName = document.getElementById('col-db-name').value;
            const oldName = document.getElementById('col-old-name').value;
            const colName = document.getElementById('col-name-input').value.trim();
            const engine = document.getElementById('col-engine-select').value;

            if (!colName) return showAlert('Error', 'El nombre es obligatorio');

            const isEdit = !!oldName;
            const url = isEdit ? `/api/db/${dbName}/collections/${oldName}/${colName}` : `/api/db/${dbName}/collections/${colName}`;
            const method = isEdit ? 'PUT' : 'POST';

            try {
                const response = await fetchWithAuth(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ engine })
                });
                if (response.ok) {
                    closeCollectionModal();
                    fetchDatabases();
                    if (engine === 'Document') {
                        selectCollection(dbName, colName, engine);
                    }
                    showAlert('Success', `Colección ${isEdit ? 'actualizada' : 'creada'} correctamente`, 'success');
                } else {
                    showAlert('Error', 'Error al procesar la colección');
                }
            } catch (e) {
                console.error('Exception in handleSubmitCollection:', e);
                showAlert('Error', 'Error de conexión: ' + e.message);
            }
        }

        function handleDeleteCol(dbName, colName) {
            document.getElementById('delete-col-db-hidden').value = dbName;
            document.getElementById('delete-col-name-hidden').value = colName;
            document.getElementById('delete-col-name-label').textContent = colName;
            document.getElementById('col-delete-modal').classList.remove('hidden');
        }

        function closeColDeleteModal() {
            document.getElementById('col-delete-modal').classList.add('hidden');
        }

        async function confirmDeleteCol() {
            const dbName = document.getElementById('delete-col-db-hidden').value;
            const colName = document.getElementById('delete-col-name-hidden').value;

            try {
                const response = await fetchWithAuth(`/api/db/${dbName}/collections/${colName}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    closeColDeleteModal();
                    fetchDatabases();
                    showAlert('Success', 'Colección eliminada', 'success');
                } else {
                    showAlert('Error', 'Error al eliminar');
                }
            } catch (e) {
                console.error('Exception in confirmDeleteCol:', e);
                showAlert('Error', 'Error de conexión: ' + e.message);
            }
        }

        async function refreshCollections(dbName, engine, btn) {
            btn.style.animation = 'spin 1s linear infinite';
            try {
                const response = await fetchWithAuth(`/api/db/${dbName}/collections`);
                if (response.ok) {
                    const cols = await safeJson(response);
                    // If backend returns objects with engine info, filter. Otherwise, show all in "Document" for now.
                    let filtered = cols;
                    if (cols.length > 0 && typeof cols[0] === 'object') {
                        filtered = cols.filter(c => c.engine === engine).map(c => c.name);
                    } else if (engine !== 'Document') {
                        filtered = []; // Default all to Document if no engine info
                    }
                    document.getElementById(`col-list-${dbName}-${engine}`).innerHTML = renderCollectionItems(dbName, filtered, engine);
                }
            } finally {
                btn.style.animation = '';
            }
        }

        function renderUserItems(dbName) {
            if (!allUsers || allUsers.length === 0) return '<div style="color: #475569; font-size: 0.75rem; padding-left: 1rem;">No users</div>';

            const dbUsers = allUsers.filter(u => {
                const userRoles = u.roles || [];
                return userRoles.some(rn => {
                    const r = allSystemRoles.find(role => role.name === rn);
                    return r && r.database && (r.database.toLowerCase() === dbName.toLowerCase());
                });
            });

            if (dbUsers.length === 0) return '<div style="color: #475569; font-size: 0.75rem; padding-left: 1rem;">No users</div>';

            return dbUsers.map(u => {
                const rolesForDb = (u.roles || []).filter(rn => {
                    const r = allSystemRoles.find(role => role.name === rn);
                    return r && r.database && (r.database.toLowerCase() === dbName.toLowerCase());
                }).map(rn => {
                    // Extract core role type from name (e.g., admin_testdb -> admin)
                    return rn.split('_')[0];
                }).join(', ');

                return `
                    <div class="tree-type-item" style="padding-left: 1rem;">
                        <svg style="width: 10px; height: 10px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                        <span>${u.username} <small style="color: #64748b">(${rolesForDb})</small></span>
                    </div>
                `;
            }).join('');
        }

        function renderDbTree(dbs) {
            const treeRoot = document.getElementById('db-tree-root');
            if (!treeRoot) return;

            // Filter databases: super-user (global) sees all, others only see where they have a role
            let filteredDbs = dbs;
            if (userProfile !== 'super-user') {
                filteredDbs = dbs.filter(db => {
                    const currentUser = allUsers.find(u => u.username === username);
                    if (!currentUser || !currentUser.roles) return false;
                    return currentUser.roles.some(rn => {
                        const r = allSystemRoles.find(role => role.name === rn);
                        return r && r.database && (r.database.toLowerCase() === db.name.toLowerCase());
                    });
                });
            }

            if (!filteredDbs || filteredDbs.length === 0) {
                treeRoot.innerHTML = '<div style="color: #475569; font-size: 0.75rem; padding: 0.5rem;">No accessible databases</div>';
                return;
            }

            treeRoot.innerHTML = filteredDbs.map(db => {
                const privs = getUserPrivileges(db.name);
                // A user can manage a DB if they are global admin OR have 'admin' role for this specific DB
                const canManageDb = privs.admin;

                return `
                <div class="tree-db-item">
                    <div class="tree-db-name ${expandedNodes.has(`children-${db.name}`) ? 'expanded' : ''}" onclick="toggleTreeChildren(this)">
                        <svg style="width: 12px; height: 12px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M9 5l7 7-7 7"></path></svg>
                        <span>${db.name}</span>
                        <div class="db-tree-actions">
                            <div class="db-action-btn" onclick="showDbInfo('${db.name}'); event.stopPropagation();" title="Information">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            </div>
                            ${canManageDb ? `
                            <div class="db-action-btn" onclick="managePermissions('${db.name}'); event.stopPropagation();" title="Security & Permissions">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
                            </div>
                            <div class="db-action-btn" onclick="editDb('${db.name}'); event.stopPropagation();" title="Edit Config / Rename">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                            </div>
                            <div class="db-action-btn delete" onclick="handleDeleteDb('${db.name}'); event.stopPropagation();" title="Delete Database">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                            </div>
                            ` : ''}
                        </div>
                    </div>
                    <div id="children-${db.name}" class="tree-children ${expandedNodes.has(`children-${db.name}`) ? 'show' : ''}">
                        <!-- Users Subtree -->
                        <div class="tree-engine-node">
                            <div class="tree-type-item" onclick="toggleEngineActions(this)">
                                <svg style="width: 12px; height: 12px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                                users
                            </div>
                            <div class="tree-children ${expandedNodes.has(`users-list-${db.name}`) ? 'show' : ''}" id="users-list-${db.name}">
                                ${renderUserItems(db.name)}
                            </div>
                        </div>

                        ${SUPPORTED_ENGINES.map(eng => {
                    let collections = db.collections || [];
                    let filtered = collections;
                    if (collections.length > 0 && typeof collections[0] === 'object') {
                        filtered = collections.filter(c => c.engine === eng.name).map(c => c.name);
                    } else if (eng.name !== 'Document') {
                        filtered = [];
                    }

                    return `
                            <div class="tree-engine-node ${expandedNodes.has(`col-list-${db.name}-${eng.name}`) ? 'active-engine' : ''}" id="engine-${db.name}-${eng.name}">
                                <div class="tree-type-item" onclick="toggleEngineActions(this)">
                                    <svg style="width: 12px; height: 12px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="${eng.icon}"></path></svg>
                                    ${eng.name}
                                </div>
                                <div class="tree-actions">
                                    ${privs.write || privs.admin ? `
                                    <div class="action-icon" title="Añadir Coleccion" onclick="openCollectionModal('${db.name}', '', '${eng.name}')">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                                    </div>
                                    ` : ''}
                                    <div class="action-icon" title="Refrescar" onclick="refreshCollections('${db.name}', '${eng.name}', this)">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
                                    </div>
                                </div>
                                <div class="tree-children tree-collection-list ${expandedNodes.has(`col-list-${db.name}-${eng.name}`) ? 'show' : ''}" id="col-list-${db.name}-${eng.name}">
                                    ${renderCollectionItems(db.name, filtered, eng.name)}
                                </div>
                            </div>
                        `}).join('')}
                    </div>
                </div>
                `;
            }).join('');
        }

        function toggleTreeChildren(el) {
            el.classList.toggle('expanded');
            const children = el.nextElementSibling;
            children.classList.toggle('show');
            if (children.id) {
                if (children.classList.contains('show')) {
                    expandedNodes.add(children.id);
                } else {
                    expandedNodes.delete(children.id);
                }
            }
        }

        function toggleEngineActions(el) {
            const engineNode = el.parentElement;
            // Disabled auto-close logic per user request
            // engineNode.parentElement.querySelectorAll('.tree-engine-node').forEach(node => { ... });

            engineNode.classList.toggle('active-engine');
            const children = engineNode.querySelector('.tree-children');
            if (children && children.id) {
                if (engineNode.classList.contains('active-engine')) {
                    expandedNodes.add(children.id);
                    children.classList.add('show');
                } else {
                    expandedNodes.delete(children.id);
                    children.classList.remove('show');
                }
            }
        }


        let nodeToStop = null;

        function closeStopModal() {
            nodeToStop = null;
            document.getElementById('stop-modal').classList.remove('show');
            document.getElementById('stop-modal').classList.add('hidden');
        }

        function stopNode(id) {
            nodeToStop = id;
            document.getElementById('stop-node-id-label').textContent = id;
            const modal = document.getElementById('stop-modal');
            modal.classList.remove('hidden');
            modal.classList.add('show');
        }

        async function confirmStopNode() {
            const nodeId = nodeToStop;
            if (!nodeId) return;

            try {
                const response = await fetchWithAuth(`/api/monitor/nodes/${nodeId}/stop`, {
                    method: 'POST'
                });
                if (response.ok) {
                    closeStopModal();

                    // Force local status update to OFFLINE for immediate UI feedback
                    const node = allNodes.find(n => n.id === nodeId);
                    if (node) {
                        node.status = 'OFFLINE';
                        // Re-render HTMX if it's currently showing
                        htmx.trigger('#node-grid', 'load');
                    }

                    showAlert('Éxito', `Solicitud de parada enviada para el nodo ${nodeId}`, 'success');

                    // Trigger HTMX refresh after a short delay to allow backend update
                    setTimeout(() => {
                        htmx.trigger('#node-grid', 'load');
                        fetchNodes();
                    }, 2000);
                } else {

                    showAlert('Error', 'Error al enviar la solicitud de parada');
                }
            } catch (error) {
                console.error('Error stopping node:', error);
                showAlert('Error', 'Error de conexión');
            }
        }


        function getRolesForUser(username) {
            const user = allUsers.find(u => u.username === username);
            if (!user || !user.roles) return [];
            return user.roles.map(rn => allSystemRoles.find(r => r.name === rn)).filter(r => r);
        }

        async function renderRoleAssignments(dbName = '') {
            const list = document.getElementById('db-role-assignment-list');

            // ALWAYS fetch fresh users and roles to avoid stale cache
            await fetchUsers();
            await fetchRoles();

            if (!allUsers || allUsers.length === 0) {
                list.innerHTML = '<div style="color: #475569; font-size: 0.75rem; text-align: center; padding: 1rem;">No hay usuarios disponibles</div>';
                return;
            }

            list.innerHTML = allUsers.map(user => {
                const isSystemAdminTarget = user.username === 'super-user';
                const roles = getRolesForUser(user.username);
                const dbRole = roles.find(r => r.database && (r.database.toLowerCase() === dbName.toLowerCase()));
                let currentType = isSystemAdminTarget ? 'admin' : 'none';

                if (dbRole) {
                    if (dbRole.name.startsWith('admin_')) currentType = 'admin';
                    else if (dbRole.name.startsWith('read-write_')) currentType = 'read-write';
                    else if (dbRole.name.startsWith('read_')) currentType = 'read';
                }

                return `
                    <div class="role-assignment-item" ${isSystemAdminTarget ? 'style="opacity: 0.8; background: rgba(99, 102, 241, 0.05);"' : ''}>
                        <span class="text-sm font-medium">${user.username}${isSystemAdminTarget ? ' <span class="text-xs text-indigo-400">(Built-in)</span>' : ''}</span>
                        <select class="role-type-select" data-username="${user.username}" ${isSystemAdminTarget ? 'disabled' : ''}>
                            <option value="none" ${currentType === 'none' ? 'selected' : ''}>Sin Acceso</option>
                            <option value="super-user" ${currentType === 'super-user' ? 'selected' : ''} ${!isSystemAdminTarget ? 'disabled' : ''}>Super User (Reserved)</option>
                            <option value="admin" ${currentType === 'admin' ? 'selected' : ''}>Administrador</option>
                            <option value="read-write" ${currentType === 'read-write' || currentType === 'writer-reader' ? 'selected' : ''}>Escritura/Lectura</option>
                            <option value="read" ${currentType === 'read' || currentType === 'reader' ? 'selected' : ''}>Solo Lectura</option>
                        </select>
                    </div>
                `;
            }).join('');
        }

        async function editDb(name) {
            const db = allDatabases.find(d => d.name === name);
            if (!db) return;

            resetDbForm();
            document.getElementById('dbOldName').value = name;
            document.getElementById('dbNameInput').value = name;
            document.getElementById('dbStorageSelect').value = db.storage;
            document.getElementById('dbFormTitle').textContent = 'Editar Base de Datos: ' + name;
            document.getElementById('dbFormDescription').textContent = 'Modifique el nombre, tipo de almacenamiento o los roles de usuario para esta base de datos.';
            document.getElementById('dbFormSubmitBtn').textContent = 'Guardar Cambios';

            await fetchUsers(); // Force refresh users to ensure we see everyone
            await fetchRoles(); // Force refresh roles to ensure we see latest assignments
            document.getElementById('db-modal').classList.remove('hidden');
            setTimeout(() => document.getElementById('dbNameInput').focus(), 50);
        }

        async function showDbInfo(name) {
            const db = allDatabases.find(d => d.name === name);
            if (!db) return;

            document.getElementById('info-db-name').textContent = db.name;
            document.getElementById('info-db-engine').textContent = db.engine || 'Multi-Model';
            document.getElementById('info-db-storage').textContent = db.storage === 'STORE' ? 'Persistente (Disco)' : 'En Memoria (RAM)';
            document.getElementById('info-db-collections').textContent = db.collections ? db.collections.length : 0;

            document.getElementById('db-info-modal').classList.remove('hidden');
        }

        function resetDbForm() {
            document.getElementById('dbOldName').value = '';
            document.getElementById('dbNameInput').value = '';
            document.getElementById('dbStorageSelect').value = 'STORE';
            document.getElementById('dbFormTitle').textContent = 'Nueva Base de Datos';
            document.getElementById('dbFormDescription').textContent = 'Provisionar una nueva base de datos multi-modelo que soporta tipos Document, Column, Graph, Vector, Object, Key-value, Geospatial, Time-Series y Files.';
            document.getElementById('dbFormSubmitBtn').textContent = 'Crear Base de Datos';
        }

        async function handleSubmitDb() {
            console.log("DEBUG: handleSubmitDb called");
            const oldName = document.getElementById('dbOldName').value;
            const name = document.getElementById('dbNameInput').value.trim();
            const engine = document.getElementById('dbEngineSelect').value;
            const storage = document.getElementById('dbStorageSelect').value;

            console.log(`DEBUG: oldName=${oldName}, name=${name}, engine=${engine}, storage=${storage}`);

            if (!name) return showAlert('Error', 'El nombre de la base de datos es obligatorio');

            const isEdit = document.getElementById('dbFormTitle').textContent.includes('Editar') || (oldName && oldName.length > 0);
            console.log("DEBUG: isEdit=" + isEdit);

            if (isEdit && !oldName) {
                return showAlert('Error', 'No se ha seleccionado ninguna base de datos para editar', 'warning');
            }

            // Duplicate check
            if (!isEdit && allDatabases && allDatabases.some(db => db.name.toLowerCase() === name.toLowerCase())) {
                return showAlert('Error', 'Ya existe una base de datos con ese nombre');
            }

            const url = isEdit ? '/api/db/' + oldName : '/api/db';
            const method = isEdit ? 'PUT' : 'POST';

            console.log(`DEBUG: Fetching URL=${url} Method=${method}`);

            try {
                const response = await fetchWithAuth(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, engine, storage })
                });

                console.log("DEBUG: First fetch status=" + response.status);

                if (response.ok) {
                    console.log("DEBUG: Database saved/updated successfully, starting role sync");

                    // Sync roles: collect all selections including 'none'
                    const roleMappings = {};
                    document.querySelectorAll('.role-type-select').forEach(select => {
                        const username = select.dataset.username;
                        const type = select.value;
                        if (username && type) {
                            roleMappings[username] = type;
                        }
                    });

                    console.log('DEBUG: Role mappings to sync:', roleMappings);

                    // We need an endpoint to sync roles
                    // Use backticks for template literal
                    const syncUrl = `/api/web-auth/databases/${name}/sync-roles`;
                    console.log("DEBUG: Syncing roles to " + syncUrl);

                    const syncResponse = await fetchWithAuth(syncUrl, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(roleMappings)
                    });

                    console.log("DEBUG: Sync fetch status=" + syncResponse.status);

                    if (!syncResponse.ok) {
                        const err = await syncResponse.text();
                        console.error('Sync roles error:', err);
                        // IMPORTANT: Do NOT swallow this error if you want the user to know it failed.
                        // However, the DB was already saved. We show a warning.
                        showAlert('Advertencia', 'Base de datos guardada, pero hubo un error asignando permisos: ' + (err || "Acceso Denegado"), 'warning');
                    } else {
                        console.log('Sync roles successful');
                    }

                    // Wrap refresh in try/catch to ensure modal closes even if refresh fails
                    try {
                        console.log("DEBUG: Refreshing metadata...");
                        await fetchUsers();
                        await fetchRoles();
                        fetchDatabases();
                    } catch (refreshErr) {
                        console.error('Error refreshing data after save:', refreshErr);
                    }

                    document.getElementById('db-modal').classList.add('hidden');
                    resetDbForm();
                    showAlert('Éxito', `Base de datos ${isEdit ? 'actualizada' : 'creada'} correctamente`, 'success');
                } else {
                    const errorText = await response.text();
                    console.error('Database save failed:', errorText);
                    showAlert('Error', 'No se pudo procesar la solicitud: ' + errorText);
                }
            } catch (e) {
                console.error('Exception submitting database:', e);
                showAlert('Error', 'Error de conexión con el servidor: ' + e.message);
            }
        }

        function showNodeResources(nodeId) {
            selectedNodeId = nodeId;
            // Extract from event target or its parent card to ensure fresh data
            const card = event.currentTarget.closest('.card');
            if (card) {
                const nodeData = {
                    id: nodeId,
                    cpuUsage: parseFloat(card.dataset.cpu || 0),
                    memoryUsage: parseInt(card.dataset.memUsed || 0),
                    memoryMax: parseInt(card.dataset.memMax || 1),
                    diskUsage: parseInt(card.dataset.diskUsed || 0),
                    diskMax: parseInt(card.dataset.diskMax || 1)
                };
                // Pre-update modal from data-attributes for instant response
                renderResourceModal(nodeData);
            }

            document.getElementById('resource-modal').classList.remove('hidden');
            updateModalContent(); // Still call this for fallback/sync
        }

        function updateProgressBar(id, percent) {
            const el = document.getElementById(id);
            if (el) el.style.width = `${Math.min(100, Math.max(0, percent))}%`;
        }

        function renderResourceModal(node) {
            const cpuVal = document.getElementById('modal-cpu-val');
            const memVal = document.getElementById('modal-mem-val');
            const diskVal = document.getElementById('modal-disk-val');

            if (cpuVal) cpuVal.textContent = `${(node.cpuUsage || 0).toFixed(1)}%`;
            updateProgressBar('modal-cpu-bar', node.cpuUsage || 0);

            const memUsedMB = ((node.memoryUsage || 0) / 1024 / 1024).toFixed(1);
            const memMaxMB = ((node.memoryMax || 1) / 1024 / 1024).toFixed(1);
            const memPercent = (node.memoryUsage / node.memoryMax * 100) || 0;
            if (memVal) memVal.textContent = `${memUsedMB} / ${memMaxMB} MB`;
            updateProgressBar('modal-mem-bar', memPercent);

            const diskUsedGB = ((node.diskUsage || 0) / 1024 / 1024 / 1024).toFixed(1);
            const diskMaxGB = ((node.diskMax || 1) / 1024 / 1024 / 1024).toFixed(1);
            const diskPercent = (node.diskUsage / node.diskMax * 100) || 0;
            if (diskVal) diskVal.textContent = `${diskUsedGB} / ${diskMaxGB} GB`;
            updateProgressBar('modal-disk-bar', diskPercent);
        }

        function closeResourceModal() {
            selectedNodeId = null;
            document.getElementById('resource-modal').classList.add('hidden');
        }

        function updateModalContent() {
            const node = allNodes.find(n => n.id === selectedNodeId);
            if (!node) return;

            document.getElementById('modal-node-id').textContent = `Recursos: ${node.id}`;
            document.getElementById('modal-cpu-val').textContent = `${(node.cpuUsage || 0).toFixed(1)}%`;
            updateProgressBar('modal-cpu-bar', node.cpuUsage || 0);

            const memUsedMB = ((node.memoryUsage || 0) / 1024 / 1024).toFixed(1);
            const memMaxMB = ((node.memoryMax || 1) / 1024 / 1024).toFixed(1);
            const memPercent = (node.memoryUsage / node.memoryMax * 100) || 0;

            document.getElementById('modal-mem-val').textContent = `${memUsedMB} / ${memMaxMB} MB`;
            updateProgressBar('modal-mem-bar', memPercent);

            const diskUsedGB = ((node.diskUsage || 0) / 1024 / 1024 / 1024).toFixed(1);
            const diskMaxGB = ((node.diskMax || 1) / 1024 / 1024 / 1024).toFixed(1);
            const diskPercent = (node.diskUsage / node.diskMax * 100) || 0;

            document.getElementById('modal-disk-val').textContent = `${diskUsedGB} / ${diskMaxGB} GB`;
            updateProgressBar('modal-disk-bar', diskPercent);

            const lastSeenSec = Math.floor((Date.now() - node.lastSeen) / 1000);
            document.getElementById('modal-last-seen').textContent = lastSeenSec < 5 ? 'ahora mismo' : `hace ${lastSeenSec}s`;
            document.getElementById('modal-status').textContent = node.status;
            document.getElementById('modal-status').style.color = (node.status === 'ONLINE' || node.status === 'ACTIVE' || node.status === 'UP') ? '#10b981' : '#ef4444';
        }

        async function handleDeleteDb(name) {
            dbToDelete = name;
            document.getElementById('delete-db-name-label').textContent = name;
            document.getElementById('delete-db-name-hidden').value = name;
            document.getElementById('delete-confirm-input').value = '';
            document.getElementById('delete-modal').classList.remove('hidden');
        }

        function closeDeleteModal() {
            dbToDelete = null;
            document.getElementById('delete-modal').classList.add('hidden');
        }

        async function confirmDeleteDb() {
            const name = document.getElementById('delete-db-name-hidden').value;
            const confirmInput = document.getElementById('delete-confirm-input').value.trim();

            if (confirmInput !== name) {
                showAlert('Error de Validación', 'El nombre ingresado no es correcto. Por favor verifíquelo antes de eliminar.', 'error');
                return;
            }

            if (!name) return;

            try {
                const response = await fetchWithAuth('/api/db/' + name, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    closeDeleteModal();
                    fetchDatabases();
                    showAlert('Eliminado', 'La base de datos ha sido eliminada permanentemente', 'success');
                } else {
                    showAlert('Error', 'Error al eliminar la base de datos');
                }
            } catch (e) {
                console.error(e);
                showAlert('Error', 'Error de red al intentar eliminar');
            }
        }

        function openChangePasswordModal() {
            document.getElementById('password-modal').classList.remove('hidden');
            document.getElementById('modal-password-msg').textContent = '';
            document.getElementById('changePasswordForm').reset();
        }

        function closeChangePasswordModal() {
            document.getElementById('password-modal').classList.add('hidden');
        }

        async function handleChangePassword(e, source = 'modal') {
            e.preventDefault();
            const msgId = source === 'modal' ? 'modal-password-msg' : 'inline-password-msg';
            const oldPassId = source === 'modal' ? 'modal-old-password' : 'inline-old-password';
            const newPassId = source === 'modal' ? 'modal-new-password' : 'inline-new-password';

            const msg = document.getElementById(msgId);
            msg.textContent = 'Updating...';
            msg.style.color = '#94a3b8';

            const oldPass = document.getElementById(oldPassId).value;
            const newPass = document.getElementById(newPassId).value;
            const user = localStorage.getItem('jettra_username') || 'Unknown';

            try {
                const response = await fetch('/api/web-auth/change-password', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username: user, oldPassword: oldPass, newPassword: newPass })
                });

                if (response.ok) {
                    msg.style.color = '#10b981';
                    msg.textContent = 'Password changed successfully.';
                    if (source === 'modal') {
                        setTimeout(closeChangePasswordModal, 1500);
                    }
                } else {
                    const text = await response.text();
                    throw new Error(text || 'Failed');
                }
            } catch (err) {
                msg.style.color = '#ef4444';
                msg.textContent = 'Error: ' + err.message;
            }
        }

        const ALLOWED_ROLE_TYPES = ['none', 'super-user', 'admin', 'read', 'read-write'];
        let allSystemRoles = [];

        async function fetchUsers() {
            try {
                const response = await fetchWithAuth('/api/web-auth/users');
                if (response.ok) {
                    allUsers = await safeJson(response) || [];
                    renderUsers(allUsers);
                }
            } catch (error) {
                console.error('Error fetching users:', error);
                document.getElementById('user-list-container').innerHTML = `<p style="color: #ef4444;">Error: ${error.message}</p>`;
            }
        }

        async function fetchRoles() {
            try {
                const response = await fetchWithAuth('/api/web-auth/roles');
                if (response.ok) {
                    allSystemRoles = await safeJson(response) || [];
                }
            } catch (error) {
                console.error('Error fetching roles:', error);
            }
        }

        function renderUsers(users) {
            const container = document.getElementById('user-list-container');
            if (!users || users.length === 0) {
                container.innerHTML = '<p class="text-xs text-gray-400">No users found.</p>';
                return;
            }

            const totalPages = Math.ceil(users.length / usersPerPage);
            if (userPage > totalPages) userPage = totalPages || 1;

            const start = (userPage - 1) * usersPerPage;
            const end = start + usersPerPage;
            const paginatedUsers = users.slice(start, end);

            // Update Pagination UI
            document.getElementById('user-page-indicator').textContent = `Page ${userPage} of ${totalPages || 1}`;
            document.getElementById('btn-prev-user-page').disabled = userPage === 1;
            document.getElementById('btn-next-user-page').disabled = userPage === totalPages || totalPages === 0;

            container.innerHTML = `
                <div style="overflow-x: auto;">
                    <table style="width: 100%; border-collapse: collapse; margin-top: 1rem; border: 1px solid rgba(255,255,255,0.05); border-radius: 0.5rem; overflow: hidden; min-width: 600px;">
                    <thead style="background: rgba(255,255,255,0.05); font-size: 0.75rem; color: #94a3b8; text-transform: uppercase;">
                        <tr>
                            <th style="padding: 0.75rem; text-align: left;">Username</th>
                            <th style="padding: 0.75rem; text-align: left;">Profile</th>
                            <th style="padding: 0.75rem; text-align: left;">Roles / DBs</th>
                            <th style="padding: 0.75rem; text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${users.map(user => {
                const userRoles = user.roles || [];
                const roleDetails = userRoles.map(rn => {
                    const r = allSystemRoles.find(role => role.name === rn);
                    return r ? `${r.name} (${r.database === '_all' ? '*' : r.database})` : rn;
                }).join(', ');

                return `
                            <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);">
                                <td style="padding: 0.75rem; font-weight: 500;">
                                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                                        <div style="width: 24px; height: 24px; border-radius: 50%; background: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 0.7rem;">${user.username[0].toUpperCase()}</div>
                                        <div>
                                            <div>${user.username}</div>
                                            <div style="font-size: 0.65rem; color: #64748b;">${user.email || 'no email'}</div>
                                        </div>
                                    </div>
                                </td>
                                <td style="padding: 0.75rem; font-size: 0.75rem;">
                                    <span style="padding: 0.2rem 0.5rem; border-radius: 999px; background: ${user.profile === 'super-user' ? 'rgba(99, 102, 241, 0.2)' : 'rgba(255,255,255,0.05)'}; color: ${user.profile === 'super-user' ? 'var(--primary)' : '#94a3b8'}; border: 1px solid rgba(255,255,255,0.1);">
                                        ${user.profile || 'end-user'}
                                    </span>
                                </td>
                                <td style="padding: 0.75rem; font-size: 0.75rem; color: #94a3b8; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${roleDetails || 'no roles'}">${roleDetails || 'no roles'}</td>
                                <td style="padding: 0.75rem; text-align: right;">
                                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                                        <button class="btn" style="padding: 0.3rem 0.6rem; background: rgba(99, 102, 241, 0.1); color: var(--primary); border: 1px solid rgba(99, 102, 241, 0.2); font-size: 0.7rem;" onclick="handleEditUser('${user.username}')">Editar</button>
                                        ${user.username !== 'super-user' && user.username !== 'admin' ? `
                                        <button class="btn" style="padding: 0.3rem 0.6rem; background: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); font-size: 0.7rem;" onclick="deleteUser('${user.username}')">Delete</button>
                                        ` : ''}
                                    </div>
                                </td>
                            </tr>
                        `}).join('')}
                    </tbody>
                </table>
            </div>
            `;
        }

        async function renderUserDbRoles(user = null) {
            const body = document.getElementById('user-db-roles-body');
            if (allDatabases.length === 0) {
                await fetchDatabases();
            }

            const dbs = [{ name: '_all' }, ...allDatabases];

            body.innerHTML = dbs.map(db => {
                let currentRole = 'none';
                if (user) {
                    const userRoleNames = user.roles || [];
                    for (let rn of userRoleNames) {
                        const r = allSystemRoles.find(role => role.name === rn);
                        if (r && r.database === db.name) {
                            // Find which type it is
                            if (rn.startsWith('admin')) currentRole = 'admin';
                            else if (rn.startsWith('read-write')) currentRole = 'read-write';
                            else if (rn.startsWith('read')) currentRole = 'read';
                            else currentRole = rn; // Fallback to raw role name if it's one of the defaults
                        }
                    }
                }

                return `
                    <tr>
                        <td style="padding: 0.75rem; color: ${db.name === '_all' ? 'var(--primary)' : 'inherit'}; font-weight: ${db.name === '_all' ? '600' : '400'};">
                            ${db.name === '_all' ? '🌐 Global Access' : db.name}
                        </td>
                        <td style="padding: 0.75rem;">
                            <select class="user-db-role-select" data-db="${db.name}">
                                ${ALLOWED_ROLE_TYPES.map(type => `
                                    <option value="${type}" ${currentRole === type ? 'selected' : ''}>${type.charAt(0).toUpperCase() + type.slice(1)}</option>
                                `).join('')}
                            </select>
                        </td>
                    </tr>
                `;
            }).join('');
        }

        async function handleEditUser(username) {
            try {
                const response = await fetchWithAuth('/api/web-auth/users');
                const users = await safeJson(response) || [];
                const user = users.find(u => u.username === username);
                if (!user) return;

                document.getElementById('user-is-edit').value = "true";
                document.getElementById('user-username').value = user.username;
                document.getElementById('user-username').disabled = true;
                document.getElementById('user-email').value = user.email || '';
                document.getElementById('user-password').value = ""; // Empty password means keep current
                document.getElementById('user-password').placeholder = "(Leave blank to keep current)";
                document.getElementById('user-form-title').textContent = "Edit User: " + username;
                document.getElementById('user-save-btn').textContent = "Save Changes";
                document.getElementById('user-cancel-btn').classList.remove('hidden');
            } catch (e) {
                console.error('Error in handleEditUser:', e);
            }
        }

        function nextUserPage() {
            const totalPages = Math.ceil(allUsers.length / usersPerPage);
            if (userPage < totalPages) {
                userPage++;
                renderUsers(allUsers);
            }
        }

        function prevUserPage() {
            if (userPage > 1) {
                userPage--;
                renderUsers(allUsers);
            }
        }

        function openUserModal() {
            resetUserForm();
            document.getElementById('user-modal').classList.add('show');
        }

        function closeUserModal() {
            document.getElementById('user-modal').classList.remove('show');
        }

        async function handleEditUser(username) {
            const user = allUsers.find(u => u.username === username);
            if (!user) return;

            resetUserForm();
            document.getElementById('user-is-edit').value = "true";
            document.getElementById('user-form-title').innerText = "Editar Usuario: " + username;
            document.getElementById('user-save-btn').innerText = "Guardar Cambios";
            document.getElementById('user-username').value = user.username;
            document.getElementById('user-username').disabled = true;
            document.getElementById('user-email').value = user.email || "";
            document.getElementById('user-profile').value = user.profile || "end-user";

            await renderUserDbRoles(user);
            document.getElementById('user-modal').classList.add('show');
        }

        function resetUserForm() {
            document.getElementById('user-is-edit').value = "false";
            document.getElementById('user-form-title').innerText = "Crear Usuario";
            document.getElementById('user-save-btn').innerText = "Crear Usuario";
            document.getElementById('user-username').value = "";
            document.getElementById('user-username').disabled = false;
            document.getElementById('user-email').value = "";
            document.getElementById('user-password').value = "";
            document.getElementById('user-password').placeholder = "Password";
            document.getElementById('user-profile').value = "end-user";

            renderUserDbRoles();
        }

        async function saveUser() {
            const username = document.getElementById('user-username').value.trim();
            const email = document.getElementById('user-email').value.trim();
            const password = document.getElementById('user-password').value.trim();
            const isEdit = document.getElementById('user-is-edit').value === "true";
            const profile = document.getElementById('user-profile').value;

            if (!username) return showAlert('Error', 'Username is required');
            if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                return showAlert('Error', 'Invalid email format');
            }
            if (!isEdit && !password) return showAlert('Error', 'Password is required for new users');

            const selects = document.querySelectorAll('.user-db-role-select');
            const selectedRoles = [];

            // Add Profile as a role for simplicity/compatibility if needed, 
            // but the backend uses the profile field separately now.
            // We still need to pass it in the JSON.
            // ...
            // Wait, the backend userData needs 'profile' field.
            // ...

            for (let s of selects) {
                const db = s.dataset.db;
                const roleType = s.value;

                if (roleType !== 'none') {
                    // Normalize role name: reader_db1, writer-reader_all, etc.
                    const roleName = `${roleType}_${db.replace(/[^a-zA-Z0-9]/g, '_')}`;
                    selectedRoles.push(roleName);

                    // Ensure this role exists in the PD
                    await ensureRoleExists(roleName, db, roleType);
                }
            }

            const userData = {
                username,
                password: password || undefined,
                email: email || null,
                roles: selectedRoles,
                profile: profile,
                forcePasswordChange: false
            };

            const url = isEdit ? `/api/web-auth/users/${username}` : '/api/web-auth/users';
            const method = isEdit ? 'PUT' : 'POST';

            try {
                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(userData)
                });

                if (response.ok) {
                    closeUserModal();
                    resetUserForm();
                    await fetchUsers();
                    await fetchDatabases(); // REFRESH THE TREE to show user in DB subtrees
                    showAlert('Éxito', `Usuario ${isEdit ? 'actualizado' : 'creado'} correctamente`, 'success');
                } else {
                    showAlert('Error', await response.text());
                }
            } catch (e) {
                showAlert('Error', 'Error de conexión');
            }
        }

        async function ensureRoleExists(roleName, db, roleType) {
            // Check if already in our cache
            if (allSystemRoles.some(r => r.name === roleName)) return;

            // Map roleType to privileges
            let privileges = ['READ'];
            if (roleType === 'admin') privileges = ['ADMIN', 'READ', 'WRITE'];
            else if (roleType === 'read-write') privileges = ['READ', 'WRITE'];
            else if (roleType === 'read') privileges = ['READ'];

            try {
                await fetch('/api/web-auth/roles', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ name: roleName, database: db, privileges })
                });
                await fetchRoles(); // Refresh cache
            } catch (e) {
                console.error("Error ensuring role exists:", e);
            }
        }

        async function deleteUser(username) {
            if (!confirm(`Are you sure you want to delete user ${username}?`)) return;
            try {
                const response = await fetchWithAuth(`/api/web-auth/users/${username}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    await fetchUsers();
                    showAlert('Success', 'User deleted', 'success');
                } else {
                    showAlert('Error', 'Failed to delete user');
                }
            } catch (e) {
                showAlert('Error', 'Connection error');
            }
        }


        // --- Permissions Management Logic ---

        async function managePermissions(dbName) {
            document.getElementById('perm-db-name-label').textContent = dbName;
            document.getElementById('perm-db-name-hidden').value = dbName;
            document.getElementById('db-permissions-modal').classList.remove('hidden');

            // Ensure users are loaded
            await fetchUsers();
            await fetchRoles();

            // Logic similar to editDb but strictly for this modal's list
            renderPermRoleList(dbName);
        }

        async function renderPermRoleList(dbName) {
            const container = document.getElementById('perm-role-list');
            if (!allUsers || allUsers.length === 0) {
                container.innerHTML = '<p style="text-align: center; color: #64748b;">No users available.</p>';
                return;
            }

            container.innerHTML = allUsers.map(user => {
                // Determine current role for this DB
                // Role naming convention: {roleType}_{dbName} (e.g. admin_db1)
                // We check if user has any role ending in _{dbName}

                let currentRoleType = 'none';
                if (user.roles) {
                    const dbRole = user.roles.find(r => r.endsWith('_' + dbName));
                    if (dbRole) {
                        // Extract type: admin_db1 -> admin
                        currentRoleType = dbRole.substring(0, dbRole.length - dbName.length - 1);
                    }
                }

                // Protection for 'admin' user (super-user)
                const isAdminUser = user.username === 'super-user';
                if (isAdminUser) currentRoleType = 'admin';

                const isLocked = isAdminUser;

                return `
                    <div class="role-assignment-item">
                        <span style="color: #e2e8f0; font-size: 0.9rem;">${user.username}</span>
                        <select class="perm-role-select" data-username="${user.username}" ${isLocked ? 'disabled style="opacity: 0.6; cursor: not-allowed;" title="Super-user privileges cannot be changed"' : ''}>
                            <option value="denied" ${currentRoleType === 'denied' || currentRoleType === 'none' ? 'selected' : ''}>Sin Acceso (Denied)</option>
                            <option value="super-user" ${currentRoleType === 'super-user' ? 'selected' : ''} ${!isAdminUser ? 'disabled' : ''}>Super User (Reserved)</option>
                            <option value="admin" ${currentRoleType === 'admin' ? 'selected' : ''}>Admin</option>
                            <option value="read" ${currentRoleType === 'read' || currentRoleType === 'reader' ? 'selected' : ''}>Solo Lectura (Read)</option>
                            <option value="read-write" ${currentRoleType === 'read-write' || currentRoleType === 'writer-reader' ? 'selected' : ''}>Escritura / Lectura (Read-Write)</option>
                        </select>
                    </div>
                `;
            }).join('');
        }

        async function savePermissions() {
            console.log('savePermissions called');
            const dbNameInput = document.getElementById('perm-db-name-hidden');
            if (!dbNameInput) {
                showAlert('Error Crítico', 'No se encontró el campo oculto de nombre de BD.');
                return;
            }
            const dbName = dbNameInput.value;

            if (!dbName) {
                showAlert('Error', 'Nombre de base de datos no encontrado.');
                return;
            }

            const roleMappings = {};
            const currentUsername = (localStorage.getItem('jettra_username') || 'unknown').trim();
            let currentUserNewRole = 'unknown';

            document.querySelectorAll('.perm-role-select').forEach(select => {
                const username = select.dataset.username;
                const type = select.value;
                if (username && type) {
                    roleMappings[username] = type;
                    if (username.toLowerCase() === currentUsername.toLowerCase()) {
                        currentUserNewRole = type;
                    }
                }
            });

            console.log('DEBUG: Permission mappings:', roleMappings);

            // Self-Lockout Prevention
            const isGlobalAdmin = (localStorage.getItem('jettra_username') === 'super-user') || (localStorage.getItem('jettra_is_admin') === 'true');

            console.log('DEBUG: isGlobalAdmin=', isGlobalAdmin, 'currentUserNewRole=', currentUserNewRole);

            // Check if user is demoting themselves
            if (!isGlobalAdmin) {
                if (currentUserNewRole === 'denied' || currentUserNewRole === 'none') {
                    return showAlert('Acción Bloqueada', 'No puedes quitarte tu propio acceso completamente. Asigna otro administrador primero o contacta al administrador global.', 'error');
                }

                if (currentUserNewRole !== 'admin' && currentUserNewRole !== 'unknown') {
                    // Warn only if demoting from Admin (presumed) to something else
                    showPermissionConfirmModal(dbName, roleMappings);
                    return;
                }
            }

            // Proceed directly
            await submitPermissions(dbName, roleMappings);
        }

        async function submitPermissions(dbName, roleMappings) {
            const syncUrl = `/api/web-auth/databases/${dbName}/sync-roles`;

            try {
                const syncResponse = await fetch(syncUrl, {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(roleMappings)
                });

                if (!syncResponse.ok) {
                    const err = await syncResponse.text();
                    console.error('Permission sync error:', err);
                    showAlert('Error', 'Error guardando permisos: ' + (err || "Acceso Denegado"));
                } else {
                    console.log('Permissions saved successfully');
                    document.getElementById('db-permissions-modal').classList.add('hidden');
                    document.getElementById('perm-confirm-modal').classList.add('hidden');
                    showAlert('Éxito', 'Permisos actualizados correctamente', 'success');
                    await fetchUsers();
                    await fetchDatabases();
                }
            } catch (e) {
                console.error('Exception saving permissions:', e);
                showAlert('Error', 'Error de conexión: ' + e.message);
            }
        }

        function showPermissionConfirmModal(dbName, roleMappings) {
            document.getElementById('perm-confirm-db').value = dbName;
            document.getElementById('perm-confirm-data').value = JSON.stringify(roleMappings);
            document.getElementById('perm-confirm-modal').classList.remove('hidden');
        }

        function closePermissionConfirmModal() {
            document.getElementById('perm-confirm-modal').classList.add('hidden');
        }

        function confirmSavePermissions() {
            const dbName = document.getElementById('perm-confirm-db').value;
            const dataStr = document.getElementById('perm-confirm-data').value;
            if (!dbName || !dataStr) return;
            const roleMappings = JSON.parse(dataStr);
            submitPermissions(dbName, roleMappings);
        }

        function logout() {
            localStorage.removeItem('jettra_token');
            window.location.href = '/login.html';
        }
    