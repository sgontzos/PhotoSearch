package photoSearch.ui;

import org.json.JSONArray;
import org.json.JSONObject;
import photoSearch.searchFunctionality.Search;
import static spark.Spark.*;

public class PhotoSearchAPI {

    public static Search API;

    public static void init(String inputFilePath) {
        API = new Search(inputFilePath);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        port(8081);

        init(args[0]);

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");
            res.type("application/json");
        });

        get("/search", (req, res) -> {

            JSONArray results = API.getMostSimilarDescriptions(req.queryParams("query").toString(), API.getCollectionSize());

            if (results == null) {
                res.status(412);
            } else if (results.isEmpty()) {
                res.status(412);
            } else {
                res.status(200);
            }

            return results;
        });

        get("/extract_entities/:id", (req, res) -> {

            JSONArray results = API.getImageEntities(req.params(":id"));
            if (results == null) {
                res.status(412);
            } else if (results.isEmpty()) {
                res.status(412);
            } else {
                res.status(200);
            }

            return results;
        });

        get("/extract_candidate_entities", (req, res) -> {

            String query = req.queryParams("query").toString();
            double confidence = Double.valueOf(req.queryParams("conf").toString());
            int support = Integer.valueOf(req.queryParams("sup").toString());
            JSONArray results = API.getQueryCandidateEntities(query, confidence, support);

            if (results == null) {
                results = new JSONArray();
                res.status(412);
            } else if (results.isEmpty()) {
                res.status(412);
            } else {
                res.status(200);
            }

            return results;
        });

        get("/extract_entity_abstract", (req, res) -> {

            String uri = req.queryParams("uri").toString();

            JSONObject result = API.getAbstract(uri);

            if (result == null) {
                result = new JSONObject();
                res.status(412);
            } else if (result.isEmpty()) {
                res.status(412);
            } else {
                res.status(200);
            }

            return result;
        });

        get("/advanced_search", (req, res) -> {

            String expansion = req.queryParams("expansion").toString();
            String query = req.queryParams("query").toString();
            String[] uris = req.queryParams("uris").toString().split(",");

            JSONArray results = API.advancedSearch(query, uris, API.getCollectionSize(), expansion);

            if (results == null) {
                results = new JSONArray();
                res.status(412);
            } else if (results.isEmpty()) {
                res.status(412);
            } else {
                res.status(200);
            }

            return results;
        });
    }
}
