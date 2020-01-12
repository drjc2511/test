package biz.smartcommerce.azure.cmdrs.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;

import biz.smartcommerce.azure.cmdrs.model.CmdrsEntry;

public class DocDbDao implements CmdrsDAO {
    // The name of our database.
    private static final String DATABASE_ID = "cmdrsDB";

    // The name of our collection.
    private static final String COLLECTION_ID = "cmdrsTest";

    // We'll use Gson for POJO <=> JSON serialization for this example.
    private static Gson gson = new Gson();

    // The DocumentDB Client
    private static DocumentClient documentClient = DocumentClientFactory.getDocumentClient();

    // Cache for the database object, so we don't have to query for it to
    // retrieve self links.
    private static Database databaseCache;

    // Cache for the collection object, so we don't have to query for it to
    // retrieve self links.
    private static DocumentCollection collectionCache;

    @Override
    public CmdrsEntry createCmdrsEntry(CmdrsEntry cmdrsEntry) {
        // Serialize the TodoItem as a JSON Document.
        Document cmdrsEntryDocument = new Document(gson.toJson(cmdrsEntry));
        try {
            // Persist the document using the DocumentClient.
        	cmdrsEntryDocument = documentClient.createDocument(
                    getCmdrsCollection().getSelfLink(), cmdrsEntryDocument, null,
                    false).getResource();
        } catch (DocumentClientException e) {
            e.printStackTrace();
            return null;
        }

        return gson.fromJson(cmdrsEntryDocument.toString(), CmdrsEntry.class);
    }

    @Override
    public CmdrsEntry readCmdrsEntry(String id) {
        // Retrieve the document by id using our helper method.
        Document cmdrsEntryDocument = getDocumentById(id);

        if (cmdrsEntryDocument != null) {
            // De-serialize the document in to a CmdrsEntry.
            return gson.fromJson(cmdrsEntryDocument.toString(), CmdrsEntry.class);
        } else {
            return null;
        }
    }

    @Override
    public List<CmdrsEntry> readCmdrsEntries() {
        List<CmdrsEntry> cmdrsEntries = new ArrayList<CmdrsEntry>();

        // Retrieve the TodoItem documents
        List<Document> documentList = documentClient
                .queryDocuments(getCmdrsCollection().getSelfLink(),
                        "SELECT * FROM root r",
                        null).getQueryIterable().toList();

        // De-serialize the documents in to CmdrsEntries.
        for (Document cmdrsEntryDocument : documentList) {
        	cmdrsEntries.add(gson.fromJson(cmdrsEntryDocument.toString(),
        			CmdrsEntry.class));
        }
        return cmdrsEntries;
    }

    @Override
    public CmdrsEntry updateCmdrsEntry(String id, boolean isComplete) {
        // Retrieve the document from the database
        Document cmdrsEntryDocument = getDocumentById(id);

        cmdrsEntryDocument.set("complete", isComplete);

        try {
            // Persist/replace the updated document.
        	cmdrsEntryDocument = documentClient.replaceDocument(cmdrsEntryDocument,
                    null).getResource();
        } catch (DocumentClientException e) {
            e.printStackTrace();
            return null;
        }

        return gson.fromJson(cmdrsEntryDocument.toString(), CmdrsEntry.class);
    }

    @Override
    public boolean deleteCmdrsEntry(String id) {
        // DocumentDB refers to documents by self link rather than id.

        // Query for the document to retrieve the self link.
        Document cmdrsEntryDocument = getDocumentById(id);

        try {
            // Delete the document by self link.
            documentClient.deleteDocument(cmdrsEntryDocument.getSelfLink(), null);
        } catch (DocumentClientException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Database getCmdrsDatabase() {
        if (databaseCache == null) {
            // Get the database if it exists
            List<Database> databaseList = documentClient
                    .queryDatabases(
                            "SELECT * FROM root r WHERE r.id='" + DATABASE_ID
                                    + "'", null).getQueryIterable().toList();

            if (databaseList.size() > 0) {
                // Cache the database object so we won't have to query for it
                // later to retrieve the selfLink.
                databaseCache = databaseList.get(0);
            } else {
                // Create the database if it doesn't exist.
                try {
                    Database databaseDefinition = new Database();
                    databaseDefinition.setId(DATABASE_ID);

                    databaseCache = documentClient.createDatabase(
                            databaseDefinition, null).getResource();
                } catch (DocumentClientException e) {
                    e.printStackTrace();
                }
            }
        }

        return databaseCache;
    }

    private DocumentCollection getCmdrsCollection() {
        if (collectionCache == null) {
            // Get the collection if it exists.
            List<DocumentCollection> collectionList = documentClient
                    .queryCollections(
                            getCmdrsDatabase().getSelfLink(),
                            "SELECT * FROM root r WHERE r.id='" + COLLECTION_ID
                                    + "'", null).getQueryIterable().toList();

            if (collectionList.size() > 0) {
                // Cache the collection object so we won't have to query for it
                // later to retrieve the selfLink.
                collectionCache = collectionList.get(0);
            } else {
                // Create the collection if it doesn't exist.
                try {
                    DocumentCollection collectionDefinition = new DocumentCollection();
                    collectionDefinition.setId(COLLECTION_ID);

                    collectionCache = documentClient.createCollection(
                    		getCmdrsDatabase().getSelfLink(),
                            collectionDefinition, null).getResource();
                } catch (DocumentClientException e) {
                    e.printStackTrace();
                }
            }
        }

        return collectionCache;
    }

    private Document getDocumentById(String id) {
        // Retrieve the document using the DocumentClient.
        List<Document> documentList = documentClient
                .queryDocuments(getCmdrsCollection().getSelfLink(),
                        "SELECT * FROM root r WHERE r.id='" + id + "'", null)
                .getQueryIterable().toList();

        if (documentList.size() > 0) {
            return documentList.get(0);
        } else {
            return null;
        }
    }

	@Override
	public CmdrsEntry createCmdrsEntry(String json) {
		 // Serialize the TodoItem as a JSON Document.
        Document cmdrsEntryDocument = new Document(json);
        try {
            // Persist the document using the DocumentClient.
        	cmdrsEntryDocument = documentClient.createDocument(
                    getCmdrsCollection().getSelfLink(), cmdrsEntryDocument, null,
                    false).getResource();
        } catch (DocumentClientException e) {
            e.printStackTrace();
            return null;
        }

        return gson.fromJson(cmdrsEntryDocument.toString(), CmdrsEntry.class);
	}

}
