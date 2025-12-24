package model;

public class Product {

    private String title;
    private String description;
    private String price;
    private String qty;
    private String uri;

    public Product() {
    }

    public Product(String title, String description, String price, String qty, String uri) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.qty = qty;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
