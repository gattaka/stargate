package cz.gattserver.stargate;

public class UIButton {

    private int id;
    private float x;
    private float y;
    private float w;
    private float h;

    public int getId() {
        return id;
    }

    public UIButton setId(int id) {
        this.id = id;
        return this;
    }

    public float getX() {
        return x;
    }

    public UIButton setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public UIButton setY(float y) {
        this.y = y;
        return this;
    }

    public float getW() {
        return w;
    }

    public UIButton setW(float w) {
        this.w = w;
        return this;
    }

    public float getH() {
        return h;
    }

    public UIButton setH(float h) {
        this.h = h;
        return this;
    }

    public boolean isHit(float x, float y) {
        return x > getX() && y > getY() && x < getX() + getW() && y < getY() + getH();
    }
}
