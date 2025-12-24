package model;

public class CountryData {

    private int flagResourceId;

    private String name;

    public CountryData(int flagResourceId, String name) {
        this.flagResourceId = flagResourceId;
        this.name = name;
    }

    public int getFlagResourceId() {
        return flagResourceId;
    }

    public void setFlagResourceId(int flagResourceId) {
        this.flagResourceId = flagResourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
