package Utils;

public enum RecordType {
    NORMAL("0"), ALTERED("1"); //Considering Risk = 0 is "NORMAL" and Risk = 1 is "ALTERED"

    private String type;

    private RecordType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }
}
