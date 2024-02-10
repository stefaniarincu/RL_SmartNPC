package ro.smartnpc.map;

public enum Schematic {

    MOVEMENT_ARENA1("movement_arena1")

    ;

    private final String fileName;

    Schematic(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
