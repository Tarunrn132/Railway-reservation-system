package com.railway.reservation.dto;

import java.util.ArrayList;
import java.util.List;

public class StationImportSummary {

    private int imported = 0;
    private int updated = 0;
    private int skipped = 0;
    private int invalid = 0;
    private List<String> errors = new ArrayList<>();

    public StationImportSummary() {
    }

    public void incrementImported() {
        this.imported++;
    }

    public void incrementUpdated() {
        this.updated++;
    }

    public void incrementSkipped() {
        this.skipped++;
    }

    public void incrementInvalid() {
        this.invalid++;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getInvalid() {
        return invalid;
    }

    public void setInvalid(int invalid) {
        this.invalid = invalid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "StationImportSummary{" +
                "imported=" + imported +
                ", updated=" + updated +
                ", skipped=" + skipped +
                ", invalid=" + invalid +
                ", errorsCount=" + errors.size() +
                '}';
    }
}
