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

### Collection Management
Located in `DashboardResource.java` and `DataExplorer.java`.
- **Add Collection**: Available via **(+)** button on the "Document (Collection)" engine node. Opens a dialog to create a new collection.
- **Manage Collections**: Each collection node features intuitive icons for **Info**, **Rename**, and **Delete**.
- **Collection Subtree**: Automatically generates a subtree for each collection containing:
  - **Record(Document)**: For document management.
  - **Index**: For index management.
  - **Sequences**: For sequence management.
  - **Rules**: For rule definitions.

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
