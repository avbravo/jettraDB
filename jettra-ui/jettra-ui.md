# Jettra UI Components Documentation

## New Components

### Card
A versatile container for content with header, body, and hover effects.
```java
Card card = new Card("my-card");
card.setTitle("Cluster Status");
card.setSubTitle("Operational");
card.addComponent(new Label("lbl", "Nodes: 3"));
```

### Navbar
Top navigation bar with brand logo and right-aligned actions.
```java
Navbar navbar = new Navbar("top-nav");
navbar.setBrandName("Jettra Manager");
navbar.addRightComponent(new Button("logout", "Logout"));
```

### Sidebar
Fixed sidebar for application menus. Supports `SidebarItem`.
```java
Sidebar sidebar = new Sidebar("main-sidebar");
sidebar.addItem(new Sidebar.SidebarItem("nav-home", "Home", "<icon-html>"));
```

### Tree
Hierarchical tree view for data exploration.
```java
Tree tree = new Tree("explorer");
TreeNode root = new TreeNode("Databases", "📁");
root.addChild(new TreeNode("Sales", "📄"));
tree.addNode(root);
```

### Modal
Flowbite-based modal dialogs.
```java
Modal modal = new Modal("delete-modal", "Confirm Delete");
modal.addComponent(new Label("msg", "Are you sure?"));
modal.addFooterComponent(new Button("confirm", "Yes, delete"));
```

### Badge
Small labels for status and categories.
```java
Badge badge = new Badge("status", "ONLINE");
badge.setColor("green");
```

### Alert
Feedback messages for the user.
```java
Alert alert = new Alert("error", "Invalid credentials");
alert.setType("danger");
```

### Breadcrumb
Navigation path indicator.
```java
Breadcrumb bc = new Breadcrumb("path");
bc.addItem("Home", "/");
bc.addItem("Users", null);
```
