package org.motechproject.nms.api.web.contract;

/**
 *
 */
public abstract class KilkariResponseUser {
    private String circle;

    public KilkariResponseUser(String circle) {
        this.circle = circle;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }
}
