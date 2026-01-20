# JettraDB NetBeans Plugin Guide

The JettraDB NetBeans Plugin allows you to manage and visualize your JettraDB cluster directly from the NetBeans IDE.

## 1. Installation

### A. From NBM Files (Recommended)
1.  **Build**:
    Run `mvn clean install` in the `plugins/jettra-netbeans` directory.
2.  **Locate NBMs**:
    Find the `.nbm` files in the `target` directories of each module:
    - `plugins/jettra-netbeans/jettra-core/target/io-jettra-core.nbm`
    - `plugins/jettra-netbeans/jettra-auth/target/io-jettra-auth.nbm`
    - `plugins/jettra-netbeans/jettra-db-admin/target/io-jettra-db-admin.nbm`
    - `plugins/jettra-netbeans/jettra-visualizer/target/io-jettra-visualizer.nbm`
3.  **Install in IDE**:
    - Open NetBeans IDE.
    - Go to `Tools` -> `Plugins`.
    - Select the `Downloaded` tab.
    - Click `Add Plugins...` and select all 4 `.nbm` files.
    - Click `Install` and follow the prompts (Accept license, allow unsigned).
    - Restart NetBeans if prompted.

## 2. Usage

Once installed, the plugin integrates into the NetBeans `Window` menu.

### Step 1: Login
1.  Go to `Window` -> `Login Window`.
2.  Enter the URL of your JettraDB PD (e.g., `localhost:8080`).
3.  Enter Username (`admin`) and Password.
4.  Click `Login`.
    - *Success*: You will see a notification with the session token.
    - *Note*: You must login first for other tools to work.

### Step 2: Manage Databases
1.  Go to `Window` -> `Database Admin`.
2.  The window displays a tree of existing databases.
3.  **Refresh**: The list refreshes automatically or on reopen.

### Step 3: Monitor Cluster
1.  Go to `Window` -> `Cluster Visualizer`.
2.  The table displays real-time status of all nodes in the cluster:
    - **Address**: Host/Port.
    - **Role**: Leader or Follower.
    - **Status**: UP/DOWN.
    - **Resources**: CPU and Memory usage.

## 3. Troubleshooting

### "Security Manager is not supported" Error (Standalone App)
The build process (`mvn clean install`) has been configured to **automatically patch** the generated application to support running on Java 25+.
It automatically removes the forbidden Security Manager flag and adds the necessary `--add-opens` flags.

If you still encounter issues:
1.  Ensure you have run `mvn clean install` *after* pulling the latest changes.
2.  The patches are applied during the `package` phase.

### Missing Menu Items
If "Window -> Login Window" etc. do not appear:
-   Ensure you have installed **all 4 NBMs**.
-   Restart NetBeans IDE after installation.
-   The menu items are registered with specific positions (100, 200, 300) in the `Window` menu.

### Missing Menu Items
If "Window -> Login Window" etc. do not appear:
-   Ensure you have installed **all 4 NBMs**.
-   Restart NetBeans IDE after installation.
-   The menu items are registered with specific positions (100, 200, 300) in the `Window` menu.
