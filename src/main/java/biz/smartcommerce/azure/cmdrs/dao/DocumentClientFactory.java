package biz.smartcommerce.azure.cmdrs.dao;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

public class DocumentClientFactory {
    private static final String HOST = "https://cmdrs.documents.azure.com:443/";
    private static final String MASTER_KEY = "gbh9inNU2DwYWPYKLFrGOEJL3984k0G0Zew8q5Jut1LW48gVXBzdAJJ6ssFq6Oj5F6h2bXYJwEn4qQMYP3s9PA==";

    private static DocumentClient documentClient;

    public static DocumentClient getDocumentClient() {
        if (documentClient == null) {
            documentClient = new DocumentClient(HOST, MASTER_KEY,
                    ConnectionPolicy.GetDefault(), ConsistencyLevel.Session);
        }

        return documentClient;
    }

}
