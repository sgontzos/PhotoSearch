/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.searchFunctionality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * This class is used to send SPARQL queries to DBpedia Live endpoint and gather
 * information for given URIs (e.g. the abstract of a URI).
 *
 * @author Sgo
 */
public class DBpediaEndpoint {

    private static final String dbo = "<http://dbpedia.org/ontology/>";
    private static final String dbp = "<http://dbpedia.org/property/>";
    private static final String dbr = "<http://dbpedia.org/resource/>";
    private static final String rdf = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    private static final String rdfs = "<http://www.w3.org/2000/01/rdf-schema#>";
    private static final String prefixes = "PREFIX dbo: " + dbo + " PREFIX dbp: " + dbp + " PREFIX dbr: " + dbr + " PREFIX rdf: " + rdf + " PREFIX rdfs: " + rdfs + " ";
    private static final String endPoint = "http://dbpedia.org/sparql";

    /**
     * Creates a DBpediaEndpoint instance and initializes Jena query system
     */
    public DBpediaEndpoint() {
        org.apache.jena.query.ARQ.init();
    }

    /**
     * Query the given endpoint using the given SPARQl query
     *
     * @param Query to be sent
     * @param Endpoint that the query will be sent to.
     * @throws Exception
     */
    public ArrayList<String> queryEndpoint(String szQuery, String szEndpoint)
            throws Exception {
        // Create a Query with the given String
        Query query = QueryFactory.create(szQuery);

        // Create the Execution Factory using the given Endpoint
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                szEndpoint, query);

        // Set Timeout
        ((QueryEngineHTTP) qexec).addParam("timeout", "100000");

        ArrayList<String> answer = new ArrayList<>();
        if (szQuery.contains("ASK")) {
            boolean ans = qexec.execAsk();
            answer.add(String.valueOf(ans));
        } else if (szQuery.contains("SELECT")) {
            ResultSet rs = qexec.execSelect();
//            if (!rs.hasNext()) {
//                System.out.println("No results");
//            }

            while (rs.hasNext()) {
                // Get Result
                QuerySolution qs = rs.next();

                // Get Variable Names
                Iterator<String> itVars = qs.varNames();

                String partialAnswer = "{";
                // Display Result
                while (itVars.hasNext()) {
                    String szVar = itVars.next().toString();
                    String szVal = qs.get(szVar).toString();

                    if (itVars.hasNext()) {
                        partialAnswer += "" + szVar + "=" + szVal + ",";
                    } else {
                        partialAnswer += "" + szVar + "=" + szVal + "}";
                    }
                }
                answer.add(partialAnswer);
            }

        } else {
            answer.add("Unrecognized Query Type");
        }
        //System.out.println(answer);
        return answer;
    } // End of Method: queryEndpoint()

    /**
     * This method returns true if the given URI is a class/type false
     * otherwise.
     *
     * @param slot_uri
     * @return the boolean value of whether the input URI is a class or not
     */
    public boolean isClass(String slot_uri) {
        try {
            slot_uri = fixURI(slot_uri);
            String isClassQuery = prefixes
                    + "ASK { "
                    + "{?entity rdf:type " + slot_uri + ".} UNION"
                    + "{?entity dbo:type " + slot_uri + ".} UNION"
                    + "{?entity dbp:type " + slot_uri + ".} UNION"
                    + "{?entity dbr:type " + slot_uri + ".}"
                    + "}";

            boolean isClass = Boolean.valueOf(this.queryEndpoint(isClassQuery, this.endPoint).get(0));

            return isClass;
        } catch (Exception ex) {
            Logger.getLogger(DBpediaEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Boolean.FALSE;
    }

    /**
     * This method fixes the input URI as regards the starting and ending arrow
     * symbols.
     *
     * @param uri to be fixed
     * @return the fixed URI, i.e. the URI starting with left arrow and ending
     * with right arrow.
     */
    public String fixURI(String uri) {

        if (uri.startsWith("<") && uri.endsWith(">")) {
            return uri;
        } else {
            uri = uri.replace("<", "").replace(">", "");
            return "<" + uri + ">";
        }
    }

    /**
     * This method returns the DBpedia abstract of the input URI.
     *
     * @param uri of which the abstract is needed.
     * @return the English string representation of the abstract
     */
    public String getAbstract(String slot_uri) {
        try {
            slot_uri = fixURI(slot_uri);
            String query = prefixes
                    + "SELECT ?abs WHERE "
                    + "{" + slot_uri + " dbo:abstract ?abs .}";

            String abs = "";
            ArrayList<String> abstracts = this.queryEndpoint(query, this.endPoint);
            for (String crtntAbs : abstracts) {
                if (crtntAbs.contains("@en")) {
                    abs = crtntAbs.replaceFirst("abs=", "");
                    break;
                }
            }

            if (abs != null) {
                return abs.substring(1, abs.length() - 1);
            } else {
                return "No info was found!";
            }
        } catch (Exception ex) {
            Logger.getLogger(DBpediaEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {

        DBpediaEndpoint ep = new DBpediaEndpoint();
        System.out.println(ep.fixURI("<http://dbpedia.org/resource/Bruce_Dickinson"));
        System.out.println(ep.getAbstract("http://dbpedia.org/resource/Bruce_Dickinson"));
        System.out.println(ep.isClass("http://dbpedia.org/resource/Bruce_Dickinson"));
        System.out.println(ep.isClass("http://dbpedia.org/resource/Country"));
    }
}
