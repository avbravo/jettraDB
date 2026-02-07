package io.jettra.driver;

import io.smallrye.mutiny.Uni;

public interface JettraClient {
    Uni<Void> save(String collection, Object document);

    Uni<Void> save(String collection, String jettraId, Object document);

    Uni<Object> findById(String collection, String id);

    Uni<Object> findById(String collection, String id, boolean resolveRefs);

    Uni<java.util.List<Object>> find(String collection, String query, int offset, int limit);

    Uni<java.util.List<Object>> find(String collection, String query, int offset, int limit, boolean resolveRefs);

    Uni<Void> delete(String collection, String jettraId);

    // MongoDB-like Operations
    Uni<Void> insertOne(String collection, Object document);

    Uni<Void> insertMany(String collection, java.util.List<Object> documents);

    Uni<Void> deleteOne(String collection, String query);

    Uni<Void> deleteMany(String collection, String query);

    Uni<Void> replaceOne(String collection, String query, Object document);

    Uni<Long> count(String collection);
    Uni<Long> count(String collection, String query);
    Uni<java.util.List<Object>> aggregate(String collection, String pipeline);
    Uni<Double> sum(String collection, String field, String query);
    Uni<Double> avg(String collection, String field, String query);
    Uni<Double> min(String collection, String field, String query);
    Uni<Double> max(String collection, String field, String query);

    Uni<String> generateJettraId(String bucketId);

    Uni<java.util.List<String>> getDocumentVersions(String collection, String jettraId);

    Uni<Void> restoreVersion(String collection, String jettraId, String version);

    Uni<Object> resolveReference(String collection, String referenceJettraId);

    Uni<Void> createDatabase(String name, String storage);

    Uni<Void> addCollection(String dbName, String colName, String engine);

    Uni<Void> deleteDatabase(String name);

    Uni<Void> renameDatabase(String oldName, String newName);

    Uni<String> getDatabaseInfo(String name);

    Uni<java.util.List<String>> listDatabases();

    Uni<java.util.List<String>> listCollections(String dbName);

    // Index Management
    Uni<Void> createIndex(String dbName, String colName, String field, String type);

    Uni<Void> deleteIndex(String dbName, String colName, String indexName);

    Uni<java.util.List<String>> listIndexes(String dbName, String colName);

    // Cluster Monitor
    Uni<Void> addCollection(String dbName, String colName);

    Uni<Void> removeCollection(String dbName, String colName);

    Uni<Void> renameCollection(String dbName, String oldName, String newName);

    Uni<java.util.List<NodeInfo>> listNodes();

    Uni<Void> stopNode(String nodeId);
    
    Uni<String> getMultiRaftGroups();

    Uni<String> login(String username, String password);

    Uni<Void> changePassword(String username, String oldPassword, String newPassword);

    // Security Management
    Uni<Void> createUser(String username, String password, String email, java.util.Set<String> roles);

    Uni<Void> updateUser(String username, String password, String email, java.util.Set<String> roles);

    Uni<java.util.List<String>> listUsers();

    Uni<Void> deleteUser(String username);

    Uni<Void> createRole(String name, String database, java.util.Set<String> privileges);

    Uni<Void> updateRole(String name, String database, java.util.Set<String> privileges);

    Uni<java.util.List<String>> listRoles();

    Uni<Void> deleteRole(String name);

    Uni<String> executeSql(String sql);

    Uni<String> executeSql(String sql, boolean resolveRefs);

    Uni<String> executeSql(String sql, int offset, int limit, boolean resolveRefs);

    // Sequence Management
    Uni<Void> createSequence(String name, String database, long start, long increment);

    Uni<Long> nextSequenceValue(String name);

    Uni<Long> currentSequenceValue(String name);

    Uni<Void> resetSequence(String name, long value);

    Uni<Void> deleteSequence(String name);

    Uni<java.util.List<String>> listSequences(String database);

    // Backup & Restore
    Uni<String> backupDatabase(String dbName, String format); // format: "json" or "native"

    Uni<Void> restoreDatabase(String dbName, String backupId, String format);

    String connectionInfo();
}
