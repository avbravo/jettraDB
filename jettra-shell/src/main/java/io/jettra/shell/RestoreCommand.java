package io.jettra.shell;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "restore", description = "Restore a specific version of a document")
public class RestoreCommand implements Runnable {

    @Parameters(index = "0", description = "Collection name")
    String collection;

    @Parameters(index = "1", description = "Document ID")
    String id;

    @Parameters(index = "2", description = "Version ID/Number")
    String version;

    @Override
    public void run() {
        if (JettraShell.authToken == null) {
            System.out.println("Error: Not logged in. Please run 'login' command first.");
            return;
        }

        System.out.println("Restoring document " + id + " to version " + version + "...");

        try {
            io.jettra.driver.JettraReactiveClient client = new io.jettra.driver.JettraReactiveClient(
                    JettraShell.pdAddress, JettraShell.authToken);
            client.restoreVersion(collection, id, version).await().indefinitely();
            System.out.println("Successfully restored version " + version);
        } catch (Exception e) {
            System.err.println("Restore failed: " + e.getMessage());
        }
    }
}
