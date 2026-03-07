package com.devmaster.goatfarm.goat.persistence.repository;

import com.devmaster.goatfarm.goat.enums.GoatBreed;

public interface GoatBreedCountProjection {

    GoatBreed getBreed();

    long getTotal();
}
