# Jettra UI Framework Guide

Jettra UI is a Java API designed to generate modern, responsive web interfaces programmatically. It abstracts away HTML and CSS (specifically Flowbite/TailwindCSS), allowing developers to build rich UIs using pure Java objects.

## Core Concepts

Jettra UI revolves around a few key concepts:
1.  **Components**: The building blocks of the UI (e.g., Buttons, Labels, Inputs).
2.  **Containers**: Components that can hold other components (e.g., Div, Form).
3.  **Templates**: Pre-defined layouts to structure your application pages.
4.  **Events & Validation**: Mechanisms to handle user interaction and data integrity.

## Getting Started

To use Jettra UI, ensure the module is included in your project's `pom.xml`.

```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>jettra-ui</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Basic Component Usage

All UI elements inherit from the base `Component` class. You can set IDs, add custom attributes, and add event listeners.

### Creating a Button

```java
import io.jettra.ui.component.Button;

// Create a button with ID "save-btn" and label "Save Changes"
Button saveBtn = new Button("save-btn", "Save Changes");

// The component comes pre-styled with optimized Flowbite CSS classes
```

### Creating Inputs and Labels

```java
import io.jettra.ui.component.InputText;
import io.jettra.ui.component.Label;

// Create a label
Label nameLabel = new Label("lbl-name", "Full Name");

// Create an input field
InputText nameInput = new InputText("inp-name");
nameInput.setPlaceholder("Enter your name");

// Create a password field manually
InputText passInput = new InputText("inp-pass");
passInput.setType("password");
```

### Using Containers

Containers like `Div` and `Form` are used to group components.

```java
import io.jettra.ui.component.Div;
import io.jettra.ui.component.Form;

Form form = new Form("registration-form");

// Add components to the form
form.addComponent(nameLabel);
form.addComponent(nameInput);
form.addComponent(saveBtn);
```

### Login Component

Jettra UI provides a high-level `Login` component to simplify the creation of login forms.

**Basic Login:**

```java
import io.jettra.ui.component.*;

InputText username = new InputText("username");
Password password = new Password("password");
Button loginBtn = new Button("btn-login", "Sign In");

// Add HTMX properties for AJAX login
loginBtn.setHxPost("/auth/login");
loginBtn.setHxTarget("body");

Login login = new Login("login-comp", username, password, loginBtn);
login.setTitle("Welcome Back");

// Render
String html = login.render();
```

**Login with Role Selection:**

```java
SelectOne roleInfo = new SelectOne("sel-role");
roleInfo.addOption("admin", "Administrator");
roleInfo.addOption("user", "User");

Login loginWithRole = new Login("login-role", username, password, loginBtn, roleInfo);
```

### Complex Components

#### SelectOne (Dropdown)

```java
import io.jettra.ui.component.SelectOne;

SelectOne roleSelect = new SelectOne("role-select");
roleSelect.addOption("USER", "Standard User");
roleSelect.addOption("ADMIN", "Administrator");

form.addComponent(roleSelect);
```

#### Data Table

```java
import io.jettra.ui.component.Table;
import java.util.Arrays;

Table userTable = new Table("users-table");

// Set headers
userTable.addHeader("ID");
userTable.addHeader("Username");
userTable.addHeader("Role");

// Add rows
userTable.addRow(Arrays.asList("1", "jdoe", "USER"));
userTable.addRow(Arrays.asList("2", "admin", "ADMIN"));
```

#### Tree (Data Explorer)

```java
import io.jettra.ui.component.Tree;

Tree tree = new Tree("explorer");
Tree.TreeNode root = new Tree.TreeNode("Databases", "📁");
root.addChild(new Tree.TreeNode("Sales", "📄"));
tree.addNode(root);
```

#### Navbar and Sidebar

```java
Navbar navbar = new Navbar("top-nav");
navbar.setBrandName("Jettra Manager");

Sidebar sidebar = new Sidebar("side-nav");
sidebar.addItem(new Sidebar.SidebarItem("home", "Home", "🏠"));
```

## Layouts and Templates

Jettra UI provides a powerful `Template` class to create responsive application layouts.

```java
Template appTemplate = new Template();
appTemplate.setTop(navbar);
appTemplate.setLeft(sidebar);
appTemplate.setCenter(content);
```

## HTMX Support

Jettra UI includes first-class support for HTMX to enable dynamic interactions with minimal JavaScript. 
The `Component` class provides helper methods to easily configure HTMX attributes.

### Example: AJAX Form Submission

```java
Button saveBtn = new Button("btn-save", "Save User");

// Configure HTMX properties
saveBtn.setHxPost("/api/users");      // URL to POST to
saveBtn.setHxTarget("#user-table");   // Element to update with the response
saveBtn.setHxSwap("outerHTML");       // Strategy for swapping content

form.addComponent(saveBtn);
```

### Available HTMX Methods

Every component supports the following methods:

- `setHxGet(String url)`
- `setHxPost(String url)`
- `setHxPut(String url)`
- `setHxDelete(String url)`
- `setHxTarget(String selector)`
- `setHxSwap(String swapStrategy)`
- `setHxTrigger(String triggerEvent)`
- `setHxConfirm(String message)`
- `setHxInclude(String selector)`

These methods automatically render the corresponding `hx-*` attributes in the generated HTML.

## Event Handling

You can attach event listeners to handle user interactions programmatically.

```java
import io.jettra.ui.event.JettraEvent;

Button actionBtn = new Button("btn-action", "Click Me");

actionBtn.addEventListener((JettraEvent event) -> {
    System.out.println("Button clicked! Source: " + event.getSource().getId());
    // Handle the event logic here
});
```

*Note: In the current snapshot, Java event listeners are logical constructs. For client-server interaction in a web environment, we recommend using the HTMX support described above or integrating with a suitable backend adapter.*
