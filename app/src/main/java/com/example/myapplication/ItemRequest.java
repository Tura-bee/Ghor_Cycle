package com.example.myapplication;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ItemRequest represents a donation request or barter/exchange proposal for a product in GhorCycle.
 *
 * Implements:
 * 1. Storage of requester, owner, product identity, and proposed terms.
 * 2. Distinction between pure donation requests and item-exchange proposals.
 * 3. Lifecycle states: PENDING, ACCEPTED, REJECTED, CANCELLED.
 * 4. Timestamps for audit trails and history tracking.
 */
@IgnoreExtraProperties
public class ItemRequest implements Serializable {

    public static final String TYPE_DONATION = "DONATION";
    public static final String TYPE_EXCHANGE = "EXCHANGE";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String requestId;
    private String productId;
    private String productTitle;
    private String ownerId;
    private String requesterId;
    private String requesterEmail;
    private String requestType;
    private String offeredItem;
    private String message;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ItemRequest() {
        this.status = STATUS_PENDING;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public ItemRequest(String requestId, String productId, String productTitle, String ownerId,
                       String requesterId, String requesterEmail, String requestType,
                       String offeredItem, String message, String status) {
        this.requestId = requestId;
        this.productId = productId;
        this.productTitle = productTitle;
        this.ownerId = ownerId;
        this.requesterId = requesterId;
        this.requesterEmail = requesterEmail;
        this.requestType = requestType != null ? requestType : TYPE_DONATION;
        this.offeredItem = offeredItem != null ? offeredItem : "";
        this.message = message != null ? message : "";
        this.status = status != null ? status : STATUS_PENDING;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public ItemRequest(String requestId, String productId, String ownerId, String requesterId,
                       String requestType, String message, String status) {
        this(requestId, productId, "Sample Item", ownerId, requesterId, "user@example.com",
             requestType, "", message, status);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getOfferedItem() {
        return offeredItem;
    }

    public void setOfferedItem(String offeredItem) {
        this.offeredItem = offeredItem;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Exclude
    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isAccepted() {
        return STATUS_ACCEPTED.equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isRejected() {
        return STATUS_REJECTED.equalsIgnoreCase(status);
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", requestId);
        map.put("productId", productId);
        map.put("productTitle", productTitle);
        map.put("ownerId", ownerId);
        map.put("requesterId", requesterId);
        map.put("requesterEmail", requesterEmail != null ? requesterEmail : "");
        map.put("requestType", requestType != null ? requestType : TYPE_DONATION);
        map.put("offeredItem", offeredItem != null ? offeredItem : "");
        map.put("message", message != null ? message : "");
        map.put("status", status != null ? status : STATUS_PENDING);
        map.put("createdAt", createdAt != null ? createdAt : Timestamp.now());
        map.put("updatedAt", Timestamp.now());
        return map;
    }
}
