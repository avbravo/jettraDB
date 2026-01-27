
        async function showDocVersions(jid) {
            document.getElementById('versions-doc-id').textContent = jid;
            const container = document.getElementById('versions-list-container');
            container.innerHTML = '<div style="color: #94a3b8; text-align: center;">Fetching versions...</div>';
            showModal('versions-modal');

            try {
                const nodesResp = await fetchWithAuth('/api/monitor/nodes');
                const nodes = await safeJson(nodesResp) || [];
                const storeNode = nodes.find(n => n.role === 'STORAGE' && n.status === 'ONLINE');

                if (!storeNode) {
                    container.innerHTML = '<div style="color: #ef4444;">Error: No storage node available.</div>';
                    return;
                }

                // Call Proxy
                const targetUrl = `http://${storeNode.address}/api/v1/document/${currentSelectedCollection}/${encodeURIComponent(jid)}/versions`;
                const resp = await fetchWithAuth('/api/db/proxy/document', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ url: targetUrl, method: 'GET' })
                });

                if (resp.ok) {
                    const data = await safeJson(resp);
                    if (!data || data.length === 0) {
                        container.innerHTML = '<div style="color: #94a3b8; font-style: italic;">No history found.</div>';
                        return;
                    }

                    // Data is usually sorted oldest to newest from backend. Reverse to show newest first.
                    let html = '<div style="display: flex; flex-direction: column; gap: 1rem;">';
                    data.reverse().forEach((vStr) => {
                        let vObj = vStr;
                        if (typeof vStr === 'string') {
                            try { vObj = JSON.parse(vStr); } catch (e) { }
                        }

                        const vNum = vObj._version || 'N/A';
                        const vDate = vObj._lastModified || 'Unknown';

                        html += `
                            <div class="card" style="padding: 1rem; border: 1px solid rgba(255,255,255,0.05); background: rgba(0,0,0,0.2);">
                                <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem; align-items: center;">
                                    <div>
                                        <span style="font-weight: 600; color: var(--secondary);">Version ${vNum}</span>
                                        <span style="font-size: 0.8rem; color: #64748b; margin-left: 0.5rem;">${vDate}</span>
                                    </div>
                                    <button class="btn" style="padding: 0.25rem 0.75rem; font-size: 0.8rem; background: rgba(99, 102, 241, 0.2); color: #a5b4fc; border: 1px solid rgba(99, 102, 241, 0.2);"
                                            onclick="openRestoreModal('${jid}', '${vNum}')">
                                        Restore
                                    </button>
                                </div>
                                <pre style="margin: 0; font-size: 0.8rem; color: #a5b4fc; overflow-x: auto; max-height: 150px;">${JSON.stringify(vObj, null, 2)}</pre>
                            </div>
                        `;
                    });
                    html += '</div>';
                    container.innerHTML = html;
                } else {
                    container.innerHTML = `<div style="color: #ef4444;">Failed to load versions (${resp.status}).</div>`;
                }
            } catch (e) {
                container.innerHTML = `<div style="color: #ef4444;">Error: ${e.message}</div>`;
            }
        }

        function openRestoreModal(jid, version) {
            document.getElementById('restore-jid-hidden').value = jid;
            document.getElementById('restore-ver-hidden').value = version;
            document.getElementById('restore-ver-label').textContent = version;
            document.getElementById('restore-modal').classList.remove('hidden');
        }

        function closeRestoreModal() {
            document.getElementById('restore-modal').classList.add('hidden');
        }

        async function confirmRestore() {
            const jid = document.getElementById('restore-jid-hidden').value;
            const version = document.getElementById('restore-ver-hidden').value;

            try {
                // We need storeNode address again. 
                const nodesResp = await fetchWithAuth('/api/monitor/nodes');
                const nodes = await safeJson(nodesResp) || [];
                const storeNode = nodes.find(n => n.role === 'STORAGE' && n.status === 'ONLINE');

                if (!storeNode) {
                    showAlert('Error: No storage node available', 'error');
                    return;
                }

                const targetUrl = `http://${storeNode.address}/api/v1/document/${currentSelectedCollection}/${encodeURIComponent(jid)}/restore/${version}`;
                const resp = await fetchWithAuth('/api/db/proxy/document', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ url: targetUrl, method: 'POST' })
                });

                if (resp.ok) {
                    showAlert(`Version ${version} restored!`, 'success');
                    closeRestoreModal();
                    closeModal('versions-modal');
                    // Refresh listing
                    loadDocuments(currentSelectedCollection);
                } else {
                    const txt = await resp.text();
                    showAlert('Restore failed: ' + txt, 'error');
                }
            } catch (e) {
                showAlert('Error: ' + e.message, 'error');
            }
        }
    