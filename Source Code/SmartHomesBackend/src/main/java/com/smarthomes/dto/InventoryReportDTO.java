package com.smarthomes.dto;

import com.smarthomes.models.Product;
import java.math.BigDecimal;

public class InventoryReportDTO extends Product {
    private int inventoryCount;
    private BigDecimal discountAmount;
    private BigDecimal rebateAmount;

    // Default constructor
    public InventoryReportDTO() {
        super();
    }

    public InventoryReportDTO(Product product) {
        super(product.getCategoryId(), product.getName(), product.getPrice(), product.getDescription(),
                product.getManufacturerName());
        this.setId(product.getId());
    }

    public int getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(int inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = (discountAmount != null) ? discountAmount : BigDecimal.ZERO;
    }

    public void setRebateAmount(BigDecimal rebateAmount) {
        this.rebateAmount = (rebateAmount != null) ? rebateAmount : BigDecimal.ZERO;
    }

    public BigDecimal getRebateAmount() {
        return rebateAmount;
    }

    @Override
    public String toString() {
        return super.toString() + " InventoryReportDTO{" +
                "inventoryCount=" + inventoryCount +
                ", discountAmount=" + discountAmount +
                ", rebateAmount=" + rebateAmount +
                '}';
    }
}