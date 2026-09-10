package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.enums.GoatBreed;

public record GoatBreedCount(GoatBreed breed, long total) {
}
