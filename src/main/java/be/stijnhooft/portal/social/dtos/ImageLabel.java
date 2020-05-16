package be.stijnhooft.portal.social.dtos;

public enum ImageLabel {

    ORIGINAL("original"),
    THUMBNAIL("thumbnail");

    private final String value;

    ImageLabel(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
