package mif24.jadedemo.model.descriptor;
import java.io.Serializable;

/**
 * Décrit la politique du village.
 * @author Yann
 */

public class VillagePolicyDescriptor implements Serializable {

    int maxChildrenPerCouple = 2;

    boolean cutTrees = false;// autorisation de couper du bois
    boolean makeFields = false;// autorisation de faire de nouveaux champs
    boolean makeHouses = false;// autorisation de construire des maisons
    public VillagePolicyDescriptor(){}

    public int getMaxChildrenPerCouple() {
        return maxChildrenPerCouple;
    }

    public void setMaxChildrenPerCouple(int maxChildrenPerCouple) {
        if (maxChildrenPerCouple < 0)
            maxChildrenPerCouple =0;
        this.maxChildrenPerCouple = maxChildrenPerCouple;
    }

    public boolean isCutTrees() {
        return cutTrees;
    }

    public void setCutTrees(boolean cutTrees) {
        this.cutTrees = cutTrees;
    }

    public boolean isMakeFields() {
        return makeFields;
    }

    public void setMakeFields(boolean makeFields) {
        this.makeFields = makeFields;
    }

    public boolean isMakeHouses() {
        return makeHouses;
    }

    public void setMakeHouses(boolean makeHouses) {
        this.makeHouses = makeHouses;
    }


}
