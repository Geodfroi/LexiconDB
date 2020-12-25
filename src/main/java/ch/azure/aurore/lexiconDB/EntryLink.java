package ch.azure.aurore.lexiconDB;

import ch.azure.aurore.sqlite.wrapper.annotations.DatabaseClass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@DatabaseClass
public class EntryLink {
    private int _id;
    private boolean _modified;

    private List<EntryContent> entries = new ArrayList<>();

    public EntryLink() {
    }

    private EntryLink(EntryContent o1, EntryContent o2) {
        entries.add(o1);
        entries.add(o2);
        entries.sort(Comparator.comparingInt(EntryContent::get_id));
    }

    public static EntryLink create(EntryContent o1, EntryContent o2) {
        EntryLink link = new EntryLink(o1, o2);
        o1.addLink(link);
        o2.addLink(link);
        return link;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public List<EntryContent> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryContent> entries) {
        this.entries = entries;
    }

    public boolean is_modified() {
        return _modified;
    }

    public void set_modified(boolean _modified) {
        this._modified = _modified;
    }

    @Override
    public String toString() {
        return "EntryLink{" +
                "entries=" + entries +
                '}';
    }


}
