package main.java.Data;

public enum BibleName {
    PLACEHOLDER,
    ASV,
    ASVS,
    BISHOPS,
    COVERDALE,
    GENEVA,
    KJV,
    KJVS,
    NET,
    TYNDALE,
    WEB;

    public String toString() {
        switch (this) {
            case PLACEHOLDER: {
                return "-- Select a Bible --";
            }
            case ASV: {
                return "American Standard Version";
            }
            case ASVS: {
                return "American Standard Version w Strongs";
            }
            case BISHOPS: {
                return "Bishops Bible";
            }
            case COVERDALE: {
                return "Coverdale Bible";
            }
            case GENEVA: {
                return "Geneva Bible";
            }
            case KJV: {
                return "Authorized King James Version";
            }
            case KJVS: {
                return "KJV with Strongs";
            }
            case NET: {
                return "NET Bible\u00ae";
            }
            case TYNDALE: {
                return "Tyndale Bible";
            }
            case WEB: {
                return "World English Bible";
            }
            default: {
                return "Error";
            }
        }
    }

    public String toFileName() {
        switch (this) {
            case PLACEHOLDER: {
                return null;
            }
            case ASV: {
                return "asv.json";
            }
            case ASVS: {
                return "asvs.json";
            }
            case BISHOPS: {
                return "bishops.json";
            }
            case COVERDALE: {
                return "coverdale.json";
            }
            case GENEVA: {
                return "geneva.json";
            }
            case KJV: {
                return "kjv.json";
            }
            case KJVS: {
                return "kjv_strongs.json";
            }
            case NET: {
                return "net.json";
            }
            case TYNDALE: {
                return "tyndale.json";
            }
            case WEB: {
                return "web.json";
            }
            default: {
                return "Error";
            }
        }
    }
}
