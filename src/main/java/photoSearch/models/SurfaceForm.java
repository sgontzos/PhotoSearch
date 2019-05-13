/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoSearch.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sgo
 */
public class SurfaceForm {

    private String name;
    private int offset;
    private List<ResourceCandidate> resources;

    public SurfaceForm(String name, int offset) {
        this.name = name.replace("\"", "");
        this.offset = offset;
        this.resources = new ArrayList<ResourceCandidate>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setResources(List<ResourceCandidate> resources) {
        this.resources = resources;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public List<ResourceCandidate> getResources() {
        return resources;
    }

    public void addResource(ResourceCandidate resource) {
        this.resources.add(resource);
    }

    @Override
    public String toString() {
        return "Name: " + this.name + ", "
                + "Offset: " + this.offset + ", "
                + "Candidate Resources: " + resources;
    }
}
