package de.wintervillage.main.shop.data;

import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;

public class ShopStatistics {

    BigDecimal sold, earned;

    public ShopStatistics() { }

    public ShopStatistics(BigDecimal sold, BigDecimal earned) {
        this.sold = sold;
        this.earned = earned;
    }

    public BigDecimal sold() {
        return this.sold;
    }

    public void sold(BigDecimal sold) {
        this.sold = sold;
    }

    public BigDecimal earned() {
        return this.earned;
    }

    public void earned(BigDecimal earned) {
        this.earned = earned;
    }

    public Document toDocument() {
        return new Document()
                .append("sold", this.sold())
                .append("earned", this.earned());
    }

    public static ShopStatistics fromDocument(Document document) {
        return new ShopStatistics(
                document.get("sold", Decimal128.class).bigDecimalValue(),
                document.get("earned", Decimal128.class).bigDecimalValue()
        );
    }

    @Override
    public String toString() {
        return "ShopStatistics{" +
                "sold=" + this.sold +
                ", earned=" + this.earned +
                '}';
    }
}
