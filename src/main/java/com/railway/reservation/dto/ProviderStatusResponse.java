package com.railway.reservation.dto;

public class ProviderStatusResponse {

    private ComponentStatus trainSearch;
    private ComponentStatus availability;
    private ComponentStatus fare;
    private ComponentStatus pnr;
    private ComponentStatus railwayBooking;
    private ComponentStatus paymentGateway;
    private ComponentStatus googleOAuth;
    private ComponentStatus sms;
    private ComponentStatus email;

    public ProviderStatusResponse() {}

    public static class ComponentStatus {
        private String provider;
        private String status; // "CONNECTED", "NOT_CONFIGURED", "ERROR"
        private String message;

        public ComponentStatus() {}

        public ComponentStatus(String status) {
            this.status = status;
        }

        public ComponentStatus(String provider, String status) {
            this.provider = provider;
            this.status = status;
        }

        public ComponentStatus(String provider, String status, String message) {
            this.provider = provider;
            this.status = status;
            this.message = message;
        }

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public ComponentStatus getTrainSearch() { return trainSearch; }
    public void setTrainSearch(ComponentStatus trainSearch) { this.trainSearch = trainSearch; }

    public ComponentStatus getAvailability() { return availability; }
    public void setAvailability(ComponentStatus availability) { this.availability = availability; }

    public ComponentStatus getFare() { return fare; }
    public void setFare(ComponentStatus fare) { this.fare = fare; }

    public ComponentStatus getPnr() { return pnr; }
    public void setPnr(ComponentStatus pnr) { this.pnr = pnr; }

    public ComponentStatus getRailwayBooking() { return railwayBooking; }
    public void setRailwayBooking(ComponentStatus railwayBooking) { this.railwayBooking = railwayBooking; }

    public ComponentStatus getPaymentGateway() { return paymentGateway; }
    public void setPaymentGateway(ComponentStatus paymentGateway) { this.paymentGateway = paymentGateway; }

    public ComponentStatus getGoogleOAuth() { return googleOAuth; }
    public void setGoogleOAuth(ComponentStatus googleOAuth) { this.googleOAuth = googleOAuth; }

    public ComponentStatus getSms() { return sms; }
    public void setSms(ComponentStatus sms) { this.sms = sms; }

    public ComponentStatus getEmail() { return email; }
    public void setEmail(ComponentStatus email) { this.email = email; }
}
