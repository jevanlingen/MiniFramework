package com.di.model;

import org.jspecify.annotations.Nullable;

public record User(@Nullable Integer id, String name) {}
