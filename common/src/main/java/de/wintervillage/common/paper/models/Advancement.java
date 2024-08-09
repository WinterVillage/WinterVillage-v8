package de.wintervillage.common.paper.models;

import java.util.Map;

public record Advancement(String key, Map<String, Long> completedCriteria) {

    @Override
    public String toString() {
        return "Advancement{" +
                "key='" + this.key + '\'' +
                ", completedCriteria=" + this.completedCriteria +
                '}';
    }
}
