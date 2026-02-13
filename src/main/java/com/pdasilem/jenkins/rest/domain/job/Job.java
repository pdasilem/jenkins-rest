package com.pdasilem.jenkins.rest.domain.job;

import com.google.gson.annotations.SerializedName;

public record Job(@SerializedName("_class") String clazz,
                  String name, String url, String color) {

    public static Job create(final String clazz, final String name, final String url, final String color) {
        return new Job(clazz, name, url, color);
    }
}
