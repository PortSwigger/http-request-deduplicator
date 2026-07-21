package com.burp.unireq.model;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.time.ZonedDateTime;

/**
 * Represents a single HTTP transaction that was deemed unique.
 * 
 * This model class encapsulates an HTTP request/response pair along with
 * metadata such as timestamp and fingerprint.
 * 
 * Key Features:
 * - Immutable request and timestamp
 * - Mutable response (set when received)
 * - Helper methods for UI display
 * 
 * @author Harshit Shah
 */
public class RequestResponseEntry {
    
    // Core data
    private final HttpRequest request;
    private HttpResponse response; // May be null initially
    private final ZonedDateTime timestamp;
    private final String fingerprint;
    private final long sequenceNumber; // Original arrival order
    
    /**
     * Creates a new RequestResponseEntry with the provided request and fingerprint.
     * The timestamp is automatically set to the current time.
     * 
     * @param request The HTTP request (must not be null)
     * @param fingerprint The unique fingerprint for this request
     * @param sequenceNumber The original sequence number for this request
     */
    public RequestResponseEntry(HttpRequest request, String fingerprint, long sequenceNumber) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (fingerprint == null || fingerprint.trim().isEmpty()) {
            throw new IllegalArgumentException("Fingerprint cannot be null or empty");
        }
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("Sequence number must be positive");
        }
        
        this.request = request;
        this.fingerprint = fingerprint;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = ZonedDateTime.now();
    }
    
    /**
     * Sets the HTTP response for this entry.
     * This method is typically called when the response is received after
     * the request has been processed and stored.
     * 
     * @param response The HTTP response (can be null)
     */
    public void setResponse(HttpResponse response) {
        this.response = response;
    }
    
    // ==================== Getters ====================
    
    /**
     * @return The original HTTP request (never null)
     */
    public HttpRequest getRequest() { 
        return request; 
    }
    
    /**
     * @return The HTTP response (may be null if not yet received)
     */
    public HttpResponse getResponse() { 
        return response; 
    }
    
    /**
     * @return The timestamp when this entry was created
     */
    public ZonedDateTime getTimestamp() { 
        return timestamp; 
    }
    
    /**
     * @return The unique fingerprint for this request
     */
    public String getFingerprint() { 
        return fingerprint; 
    }
    
    /**
     * @return The original sequence number for this request
     */
    public long getSequenceNumber() { 
        return sequenceNumber; 
    }
    
    // ==================== UI Helper Methods ====================
    
    /**
     * @return The HTTP method (GET, POST, etc.)
     */
    public String getMethod() { 
        return request.method(); 
    }
    
    /**
     * @return The request path
     */
    public String getPath() { 
        return request.path(); 
    }
    
    /**
     * @return The HTTP status code or "Pending" if no response
     */
    public String getStatusCode() { 
        return response != null ? String.valueOf(response.statusCode()) : "Pending";
    }
    
    /**
     * @return Formatted timestamp for UI display (HH:mm:ss format)
     */
    public String getFormattedTimestamp() {
        return timestamp.toLocalTime().toString();
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RequestResponseEntry that = (RequestResponseEntry) obj;
        return fingerprint.equals(that.fingerprint);
    }
    
    @Override
    public int hashCode() {
        return fingerprint.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("RequestResponseEntry{method='%s', path='%s', status='%s', timestamp='%s'}", 
            getMethod(), getPath(), getStatusCode(), getFormattedTimestamp());
    }
}
