# Database Creation Implementation with Jettra UI

This document describes the implementation of the "Create Database" functionality in the `jettra-example-ui` project using only Jettra UI components, without direct HTML or JavaScript.

## Implementation Details

### 1. Model
A `Database` model class was created to represent the database entity:
- `name`: Database name.
- `engine`: Selected engine (Multi-Model, Document, etc.).
- `storage`: Storage type (Persistent, In-Memory).

### 2. UI Component: `DatabaseForm`
The `DatabaseForm` extends `io.jettra.ui.component.Form` and encapsulates all the logic for rendering the creation form:
- Uses `InputText` for the database name.
- Uses `SelectOne` for Engine and Storage selection.
- Styles are applied via `setStyleClass()` using Tailwind CSS classes.
- HTMX attributes (`hx-post`, `hx-target`) are configured using component methods.

### 3. Backend Integration: `PlacementDriverClient`
The MicroProfile REST Client was updated to include database endpoints:
- `List<Database> getDatabases()`
- `Response createDatabase(Database db)`
- `Response updateDatabase(String oldName, Database db)`
- `Response deleteDatabase(String name)`

### 4. Controller: `DatabaseResource`
A new JAX-RS resource handles the UI flow:
- `GET /dashboard/database/new`: Returns the rendered `DatabaseForm`.
- `POST /dashboard/database/save`: Processes the form submission, calls the PD API, and returns a success `Alert` component or re-renders the form with errors.

## Components Created/Used

- **Form**: Main container for inputs.
- **InputText**: Used for database name.
- **SelectOne**: Used for dropdown selections.
- **Div/Label**: For layout and labeling.
- **Button**: For form submission and navigation.
- **Alert**: For success/error feedback.

## Wiring

The "Add Database" button in the dashboard sidebar was connected to the new resource using HTMX:
```java
addDbBtn.setHxGet("/dashboard/database/new");
addDbBtn.setHxTarget("#main-content-view");
```
