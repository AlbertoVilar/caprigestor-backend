package com.devmaster.goatfarm.events.model.repository;

import com.devmaster.goatfarm.events.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
