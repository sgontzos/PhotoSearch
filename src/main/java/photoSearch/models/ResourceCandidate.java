/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.models;

import java.util.ArrayList;
import java.util.List;
import static photoSearch.models.Constants.COMMA;
import static photoSearch.models.Prefixes.dbr;

/**
 *
 * @author Sgo
 */
public class ResourceCandidate {
    private String uri;
    private String label;
    private String contextualScore;
    private String percentageOfSecondRank;
    private String support;
    private String priorScore;
    private String finalScore;
    private String types;

    public ResourceCandidate(String uri, String label, String contextualScore, String percentageOfSecondRank,
            String support, String priorScore, String finalScore, String types) {
        this.uri = dbr.substring(1, dbr.length() - 1) + uri.replace("\"", "");
        this.label = label.replace("\"", "");
        this.contextualScore = contextualScore.replace("\"", "");
        this.percentageOfSecondRank = percentageOfSecondRank.replace("\"", "");
        this.support = support.replace("\"", "");
        this.priorScore = priorScore.replace("\"", "");
        this.finalScore = finalScore.replace("\"", "");
        this.types = types.replace("\"", "");
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setContextualScore(String contextualScore) {
        this.contextualScore = contextualScore;
    }

    public void setPercentageOfSecondRank(String percentageOfSecondRank) {
        this.percentageOfSecondRank = percentageOfSecondRank;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public void setPriorScore(String priorScore) {
        this.priorScore = priorScore;
    }

    public void setFinalScore(String finalScore) {
        this.finalScore = finalScore;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public String getContextualScore() {
        return contextualScore;
    }

    public String getPercentageOfSecondRank() {
        return percentageOfSecondRank;
    }

    public String getSupport() {
        return support;
    }

    public String getPriorScore() {
        return priorScore;
    }

    public String getFinalScore() {
        return finalScore;
    }

    public String getTypes() {
        return types;
    }

    public List<String> typesList() {

        ArrayList<String> typesAsList = new ArrayList<>();
        if (types != null && !types.isEmpty()) {
            String[] typesAsArray = types.split(COMMA);
            for (String type : typesAsArray) {
                typesAsList.add(type.trim());
            }
        }

        return typesAsList;
    }

    public int compareTo(ResourceCandidate candidate) {
        if (Double.valueOf(this.finalScore) < Double.valueOf(candidate.getFinalScore())) {
            return -1;
        } else if (Double.valueOf(candidate.getFinalScore()) < Double.valueOf(this.finalScore)) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "URI: " + this.uri + ", Categories: " + this.types;
    }

}
