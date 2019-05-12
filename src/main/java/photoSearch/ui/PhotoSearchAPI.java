package photoSearch.ui;

import org.json.JSONArray;
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
            } else {
                res.status(200);
            }

            return results;
        });

        get("/extract_entities/:id", (req, res) -> {

            JSONArray results = API.getImageEntities(req.params(":id"));
            if (results == null) {
                res.status(412);
            } else {
                res.status(200);
            }

            return results;
        });

    }
}
