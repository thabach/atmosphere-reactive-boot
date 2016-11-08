
package com.yulplay.reactive.boot.schema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "userId",
    "avatarUrl",
    "firstName",
    "lastName",
    "language",
    "clock",
    "text"
})
public class Message {

    @JsonProperty("userId")
    private String userId;
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("language")
    private String language;
    @JsonProperty("text")
    private String text;
    @JsonProperty("clock")
    private String clock;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     *     The clock
     */
    @JsonProperty("clock")
    public String getClock() {
        return clock;
    }

    /**
     *
     * @param userId
     *     The userId
     */
    @JsonProperty("clock")
    public void setClock(String clock) {
        this.clock = clock;
    }

    /**
     * 
     * @return
     *     The userId
     */
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    /**
     * 
     * @param userId
     *     The userId
     */
    @JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 
     * @return
     *     The avatarUrl
     */
    @JsonProperty("avatarUrl")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 
     * @param avatarUrl
     *     The avatarUrl
     */
    @JsonProperty("avatarUrl")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 
     * @return
     *     The firstName
     */
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    /**
     * 
     * @param firstName
     *     The firstName
     */
    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     * @return
     *     The lastName
     */
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    /**
     * 
     * @param lastName
     *     The lastName
     */
    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     * @return
     *     The language
     */
    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    /**
     * 
     * @param language
     *     The language
     */
    @JsonProperty("language")
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * 
     * @return
     *     The text
     */
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    /**
     * 
     * @param text
     *     The text
     */
    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(userId).append(avatarUrl).append(firstName).append(lastName).append(language).append(text).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Message) == false) {
            return false;
        }
        Message rhs = ((Message) other);
        return new EqualsBuilder().append(userId, rhs.userId).append(avatarUrl, rhs.avatarUrl).append(firstName, rhs.firstName).append(lastName, rhs.lastName).append(language, rhs.language).append(text, rhs.text).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
