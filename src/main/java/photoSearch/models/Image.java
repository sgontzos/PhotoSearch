/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.models;

/**
 *
 * @author Sgo
 */
public class Image {

    private String id;
    private String description;
    private String parseDescription;
    private String link;
    private String parsedLink;
    private String linkNL;
    private double score;


    public Image(String id, String description, String link) {
        this.id = id;
        this.description = description;
        this.link = link;
        this.score = score;
    }

    public Image(String id, String description, String parseDescription, String link, String parsedLink, String linkNL) {
        this.id = id;
        this.description = description;
        this.parseDescription = parseDescription;
        this.link = link;
        this.parsedLink = parsedLink;
        this.linkNL = linkNL;
        this.score = score;
    }

    public String getLinkNL() {
        return linkNL;
    }

    public void setLinkNL(String linkNL) {
        this.linkNL = linkNL;
    }

    public String getParsedLink() {
        return parsedLink;
    }

    public void setParsedLink(String parsedLink) {
        this.parsedLink = parsedLink;
    }

    public void setParseDescription(String parseDescription) {
        this.parseDescription = parseDescription;
    }

    public String getParseDescription() {
        return parseDescription;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return this.score;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return this.link;
    }

    public int compareTo(Image img) {
        if (this.score < img.getScore()) {
            return -1;
        } else if (img.getScore() < this.score) {
            return 1;
        }
        return 0;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
