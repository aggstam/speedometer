// -------------------------------------------------------------
//
// This is the Violation Structure used by the application.
// Violation data: Latitude, Longitude, Speed and Timestamp.
//
// Author: Aggelos Stamatiou, July 2020
//
// --------------------------------------------------------------

package com.stamatiou.violation;

import java.util.Date;

public class Violation {

    private Double latitude;
    private Double longitude;
    private Float speed;
    private Date timestamp;

    public static class Builder {

        private Double latitude;
        private Double longitude;
        private Float speed;
        private Date timestamp;

        public Builder() {}

        public Builder withLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder withLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder withSpeed(Float speed) {
            this.speed = speed;
            return this;
        }

        public Builder withTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Violation build() {
            Violation violation = new Violation();
            violation.latitude = this.latitude;
            violation.longitude = this.longitude;
            violation.speed = this.speed;
            violation.timestamp = this.timestamp;
            return violation;
        }
    }

    private Violation() {}

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Float getSpeed() {
        return speed;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Violation{latitude=" + latitude + ", longitude=" + longitude + ", speed=" + speed + ", timestamp=" + timestamp + "}";
    }
}
