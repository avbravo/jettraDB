package io.jettra.driver;

import io.smallrye.mutiny.Uni;

public interface JettraClient {
    Uni<Void> save(String collection, Object document);

    Uni<Object> findById(String collection, String id);

    Uni<Void> delete(String collection, String id);

    Uni<Long> count(String collection);

    Uni<Void> createDatabase(String name, String storage);

    Uni<Void> addCollection(String dbName, String colName, String engine);

    Uni<Void> deleteDatabase(String name);

    Uni<Void> renameDatabase(String oldName, String newName);

    Uni<java.util.List<String>> listDatabases();

    Uni<java.util.List<String>> listCollections(String dbName);

    Uni<Void> addCollection(String dbName, String colName);

    Uni<Void> removeCollection(String dbName, String colName);

    Uni<Void> renameCollection(String dbName, String oldName, String newName);

    Uni<java.util.List<NodeInfo>> listNodes();

    Uni<Void> stopNode(String nodeId);

    Uni<String> login(String username, String password);

    // Security Management
    Uni<Void> createUser(String username, String password, java.util.Set<String> roles);

    Uni<Void> updateUser(String username, String password, java.util.Set<String> roles);

    Uni<java.util.List<String>> listUsers();

    Uni<Void> deleteUser(String username);

    Uni<Void> createRole(String name, String database, java.util.Set<String> privileges);

    Uni<Void> updateRole(String name, String database, java.util.Set<String> privileges);

    Uni<java.util.List<String>> listRoles();

    Uni<Void> deleteRole(String name);

    String connectionInfo();
}
