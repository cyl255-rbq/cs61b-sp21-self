package byow.Core;

public record Positon(int x, int y) {

    public Positon shiftPosition(int x, int y) {
        return new Positon(this.x + x, this.y + y);
    }

}
