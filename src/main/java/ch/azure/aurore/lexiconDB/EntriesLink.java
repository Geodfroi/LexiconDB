package ch.azure.aurore.lexiconDB;

import java.util.Objects;

public class EntriesLink {

    int minID;
    int maxID;

    public EntriesLink(int id1, int id2) {
        this.minID=Math.min(id1, id2);
        this.maxID=Math.max(id1, id2);
    }

    public int getMinID() {
        return minID;
    }

    public int getMaxID() {
        return maxID;
    }

    public int getOtherID(int id) {
        if (minID == id){
            return maxID;
        }
        return minID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EntriesLink that = (EntriesLink) o;
        return minID == that.minID && maxID == that.maxID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minID, maxID);
    }

    @Override
    public String toString() {
        return "EntriesLink{" +
                "minID=" + minID +
                ", maxID=" + maxID +
                '}';
    }
}