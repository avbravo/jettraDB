# Jettra Example UI Implementation

This project implements a web interface for JettraDB using **Jettra UI** Java components, mirroring the functionality of the original `index.html` and `login.html`.

## Key Implementations

### Premium Login Page
Located in `LoginResource.java`. 
- Uses a gradient background and glassmorphism effect (blur).
- Implements a centered login card with animated transitions.
- Integrates with `AuthResource` via HTMX.

### Dashboard with Data Explorer
Located in `DashboardResource.java`.
- **Navbar**: Standard top navigation with brand and user info.
- **Sidebar**: Features a professional menu and a **Data Explorer Tree**.
- **Cluster View**: Displays node status using `Card` and `Badge` components.
- **Dynamic Content**: Uses HTMX to handle navigation and updates.

### Authentication Flow
Managed by `AuthResource.java`.
- Handles `POST` requests from the login form.
- Sets session cookies on success.
- Redirects to `/dashboard` using `HX-Redirect`.

## Component Migration
| Feature in HTML | Jettra UI Component |
| --- | --- |
| `<nav class='top-bar'>` | `Navbar` |
| `<div class='sidebar'>` | `Sidebar` |
| `<div id='db-tree-root'>` | `Tree` |
| `<div class='card'>` | `Card` |
| `<div id='alert-modal'>` | `Modal` |
| `<span class='status-online'>` | `Badge` (color green) |
| `<div id='error-msg'>` | `Alert` (type danger) |
