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
import static photoSearch.models.Prefixes.*;

/**
 * This class is used to send SPARQL queries to DBpedia Live endpoint and gather
 * information for given URIs (e.g. the abstract of a URI).
 *
 * @author Sgo
 */
public class DBpediaEndpoint {

    private static final String prefixes = "PREFIX dbo: " + dbo + " PREFIX dbp: " + dbp + " PREFIX dbr: " + dbr + " PREFIX rdf: " + rdf + " PREFIX rdfs: " + rdfs + " PREFIX dct: " + dct + " PREFIX dbc: " + dbc + " ";
    //private static final String endPoint = "http://dbpedia.org/sparql";
    private static final String endPoint = "http://dbpedia-live.openlinksw.com/sparql";

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

    /**
     * This method returns the DBpedia types of the input URI.
     *
     * @param uri of which the types are needed.
     * @return the English string representation of the types
     */
    public ArrayList<String> getTypes(String slot_uri) {
        try {
            slot_uri = fixURI(slot_uri);
            String query = prefixes
                    + "SELECT ?type WHERE {"
                    + "{" + slot_uri + " dbo:type ?type .} UNION "
                    + "{" + slot_uri + " dbr:type ?type .} UNION "
                    + "{" + slot_uri + " dbp:type ?type .} UNION "
                    + "{" + slot_uri + " rdf:type ?type .} "
                    + "}";

            ArrayList<String> types = this.queryEndpoint(query, this.endPoint);

            ArrayList<String> typesClean = new ArrayList<>();

            if (types != null) {
                for (String crntType : types) {
                    typesClean.add(crntType.substring(6, crntType.length() - 1));
                }
                return typesClean;
            }
        } catch (Exception ex) {
            Logger.getLogger(DBpediaEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This method extracts the DBpedia subjects (topics, categories) of the
     * input URI.
     *
     * @param slot_uri
     * @return ArrayList of the string representations of the subjects of     * the input URI
     */
    public ArrayList<String> getSubjects(String slot_uri) {
        try {
            slot_uri = fixURI(slot_uri);
            String query = prefixes
                    + "SELECT ?sbj WHERE {"
                    + slot_uri + " dct:subject ?sbj . "
                    + "}";

            ArrayList<String> subjects = this.queryEndpoint(query, this.endPoint);

            ArrayList<String> subjectsClean = new ArrayList<>();

            if (subjects != null) {
                for (String crntSbj : subjects) {
                    subjectsClean.add(crntSbj.substring(5, crntSbj.length() - 1));
                }
                return subjectsClean;
            }
        } catch (Exception ex) {
            Logger.getLogger(DBpediaEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This method checks whether the input resource (first parameter) is
     * subClass of the input superClass (second parameter).
     *
     * @param Class resource to be checked if is subClass of the superClass
     * @param superClass resource to be checked if is superClass of the Class
     * @return true if Class is subClass of superClass, false otherwise
     */
    public boolean isSuperClass(String resource, String superClass) {
        try {
            resource = fixURI(resource);
            superClass = fixURI(superClass);
            String isSuperClassQuery = prefixes
                    + "ASK { "
                    + resource + " rdfs:subClassOf* " + superClass + " . "
                    + "}";

            boolean isSuperClass = Boolean.valueOf(this.queryEndpoint(isSuperClassQuery, this.endPoint).get(0));

            return isSuperClass;
        } catch (Exception ex) {
            Logger.getLogger(DBpediaEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Boolean.FALSE;
    }

    public static void main(String[] args) throws IOException {

        DBpediaEndpoint ep = new DBpediaEndpoint();
        System.out.println(ep.fixURI("<http://dbpedia.org/resource/Bruce_Dickinson"));
        System.out.println(ep.getAbstract("http://dbpedia.org/resource/Bruce_Dickinson"));
        System.out.println(ep.isClass("http://dbpedia.org/resource/Bruce_Dickinson"));
        System.out.println(ep.isClass("http://dbpedia.org/resource/Country"));
        System.out.println(ep.getTypes("http://dbpedia.org/resource/United_States"));
        System.out.println(ep.getSubjects("http://dbpedia.org/resource/United_States"));
        System.out.println(ep.isSuperClass("http://dbpedia.org/ontology/MusicalArtist", "http://xmlns.com/foaf/0.1/Person"));

    }
}
