
        const token = localStorage.getItem('jettra_token');
        if (!token && !window.location.pathname.endsWith('login.html')) {
            window.location.href = '/login.html';
        }

        // Global HTMX error handling for session timeout
        document.addEventListener('htmx:responseError', (event) => {
            if (event.detail.xhr.status === 401 || event.detail.xhr.status === 403) {
                localStorage.removeItem('jettra_token');
                window.location.href = '/login.html';
            }
        });
    